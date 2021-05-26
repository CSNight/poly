package csnight.spider.poly.logic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import csnight.spider.poly.model.*;
import csnight.spider.poly.rest.dto.OrderDto;
import csnight.spider.poly.utils.ReflectUtils;
import csnight.spider.poly.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class ClawBus {
    private static ClawBus ourInstance;
    private final Logger _log = LoggerFactory.getLogger(ClawBus.class);
    private ShowService service;
    private ShowDetail showDetail;
    //TODO save seat for cache
    private Map<String, List<SeatInfo>> seatWithClass = new ConcurrentHashMap<>();
    private LinkedBlockingQueue<OrderInfo> orderQueue = new LinkedBlockingQueue<>();
    private AtomicLong timeSeatLock = new AtomicLong(0);
    private ReentrantLock lock = new ReentrantLock();
    private ScheduledExecutorService seatRefreshPool;
    private LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(4000);
    private ThreadPoolExecutor clawPool;
    private final int RETRY_TIME = 3;
    private AtomicReference<OrderPayInfo> orderPay = new AtomicReference<>();
    private AtomicBoolean stopSign = new AtomicBoolean(false);

    public static ClawBus getIns() {
        if (ourInstance == null) {
            synchronized (ClawBus.class) {
                if (ourInstance == null) {
                    ourInstance = new ClawBus();
                }
            }
        }
        return ourInstance;
    }

    public void setShowDetail(ShowDetail showDetail) {
        this.showDetail = showDetail;
        this.seatWithClass.clear();
    }

    private ClawBus() {
        service = ReflectUtils.getBean(ShowService.class);

    }

    private void GetClassSeat() {
        long st = System.currentTimeMillis();
        if (this.lock.tryLock()) {
            if (timeSeatLock.get() < st) {
                timeSeatLock.set(st);
            }
            this.lock.unlock();
        } else {
            _log.error("lock update seat failed");
            return;
        }
        List<SeatInfo> seats = service.GetSeatList(showDetail.getProjectId(), showDetail.getShowId(), showDetail.getSectionId());
        if (timeSeatLock.getAcquire() > st) {
            _log.info("old request block " + st + "->" + timeSeatLock.getAcquire());
            return;
        }
        if (seats == null) {
            _log.error("seat request failed");
            return;
        }
        if (seats.size() == 0) {
            WebSocketServer.getInstance().broadcast("票暂时售罄，继续刷新中... \n如需终止请点击停止按钮");
            return;
        }
        seatWithClass.clear();
        for (SeatInfo seat : seats) {
            if (seatWithClass.containsKey(String.valueOf(seat.getPid()))) {
                seatWithClass.get(String.valueOf(seat.getPid())).add(seat);
            } else {
                List<SeatInfo> seatInfos = new ArrayList<>();
                seatInfos.add(seat);
                seatWithClass.put(String.valueOf(seat.getPid()), seatInfos);
            }
        }
    }

    private void GenerateOrder(OrderDto dto) {
        while (!stopSign.get()) {
            if (!orderQueue.isEmpty() || seatWithClass.isEmpty()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (!dto.isAutoDownGrade()) {
                dto.setClazz(dto.getLevel());
            } else {
                if (dto.getClazz() == 0) {
                    dto.setClazz(dto.getLevel());
                } else {
                    int priceNext = getDownGradePrice(dto.getClazz());
                    if (priceNext == 0) {
                        dto.setClazz(dto.getLevel());
                    } else {
                        dto.setClazz(priceNext);
                    }
                }
            }
            _log.info("产生座位等级Id为" + dto.getClazz() + "的订单列");
            GenerateOrderInfo(dto);
        }
    }

    private String RetryMethod(Function<OrderInfo, String> func, OrderInfo t) {
        for (int i = 0; i < RETRY_TIME; i++) {
            try {
                String res = func.apply(t);
                if (!res.equals("failed")) {
                    return res;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "failed";
    }

    private int getDownGradePrice(int oldPid) {
        List<TickPrice> prices = showDetail.getTicketPriceList();
        for (int i = 0; i < prices.size(); i++) {
            if (oldPid == prices.get(i).getPriceId() && (i + 1) < prices.size()) {
                return prices.get(i + 1).getPriceId();
            }
        }
        return 0;
    }

    private void Job(OrderInfo orderInfo) {
        try {
            if (orderPay.get() != null) {
                return;
            }
            String resCommit = RetryMethod(service::CommitOnSeat, orderInfo);
            if (resCommit.equals("failed")) {
                return;
            }
            _log.info("提交座位成功；座位信息：" + orderInfo.getPriceList().getJSONObject(0).toString());
            WebSocketServer.getInstance().broadcast("提交座位成功；座位信息：" + orderInfo.getPriceList().getJSONObject(0).toString());
            String resJump = RetryMethod(service::CreateJump, orderInfo);
            if (resJump.equals("failed")) {
                return;
            }
            _log.info("提交订单跳转成功；联系方式为：" + orderInfo.getGetTicketPhone());
            WebSocketServer.getInstance().broadcast("提交订单跳转成功；联系方式为：" + orderInfo.getGetTicketPhone());
            String resOrder = RetryMethod(service::CreateOrder, orderInfo);
            if (resOrder.equals("failed")) {
                return;
            }
            OrderPayInfo payInfo = JSONObject.parseObject(resOrder, OrderPayInfo.class);
            _log.warn("订单已产生；订单号：" + payInfo.getOrderId());
            WebSocketServer.getInstance().broadcast("订单已产生；订单号：" + payInfo.getOrderId());
            if (orderPay.get() == null) {
                orderPay.set(payInfo);
                stopSign.set(true);
                seatRefreshPool.shutdown();
                WebSocketServer.getInstance().broadcast("恭喜抢票成功");
                WebSocketServer.getInstance().broadcast("订单$" + resOrder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void GenerateOrderInfo(OrderDto dto) {
        List<SeatInfo> seatInfos = seatWithClass.get(String.valueOf(dto.getClazz()));
        if (seatInfos == null) {
            return;
        }
        for (SeatInfo st : seatInfos) {
            OrderInfo order = new OrderInfo();
            order.setProjectId(showDetail.getProjectId());
            order.setShowId(showDetail.getShowId());
            order.setShowTime(showDetail.getShowTime());
            order.setGetTicketName(dto.getGetTickName());
            order.setWatchers(dto.getShowWatcher());
            order.setPayWay(dto.getPayWay());
            JSONArray array = JSONArray.parseArray("[{\"count\":1,\"freeTicketCount\": 1,\"priceId\":"
                    + dto.getClazz() + ",\"seat\":" + st.getSid() + "}]");
            order.setPriceList(array);
            orderQueue.offer(order);
        }
    }

    public void StartClaw(OrderDto dto) {
        CleanStatus();
        seatRefreshPool = Executors.newScheduledThreadPool(2);
        clawPool = new ThreadPoolExecutor(8, 50, 0L, TimeUnit.MILLISECONDS, workQueue);
        seatRefreshPool.scheduleAtFixedRate(this::GetClassSeat, 0, 4000, TimeUnit.MILLISECONDS);
        seatRefreshPool.scheduleAtFixedRate(this::GetClassSeat, 2000, 4000, TimeUnit.MILLISECONDS);
        WebSocketServer.getInstance().broadcast("抢票启动成功！");
        if (System.currentTimeMillis() < showDetail.getSaleBeginTime()) {
            WebSocketServer.getInstance().broadcast("还未开始售票，轮训等待中...");
            while (System.currentTimeMillis() < showDetail.getSaleBeginTime()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        WebSocketServer.getInstance().broadcast("抢票开始");
        clawPool.submit(() -> GenerateOrder(dto));
        clawPool.submit(() -> {
            while (!stopSign.get()) {
                try {
                    if (workQueue.remainingCapacity() < 10) {
                        Thread.sleep(500);
                        continue;
                    }
                    OrderInfo orderInfo = orderQueue.poll();
                    if (orderInfo == null) {
                        Thread.sleep(500);
                        continue;
                    }
                    Thread.sleep((long) (100 + Math.random() * 100));
                    clawPool.submit(() -> {
                        Job(orderInfo);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            WebSocketServer.getInstance().broadcast("抢票停止");
        });
    }

    private void CleanStatus() {
        workQueue.clear();
        orderPay.set(null);
        orderQueue.clear();
        stopSign.set(false);
    }

    public void StopClaw() {
        if (stopSign.get()) {
            WebSocketServer.getInstance().broadcast("等待停止中，请勿重复点击");
            return;
        }
        stopSign.set(true);
        seatRefreshPool.shutdown();
        clawPool.getQueue().clear();
        clawPool.shutdown();
        WebSocketServer.getInstance().broadcast("抢票停止中...");
        while (true) {
            if (seatRefreshPool.isShutdown() && clawPool.isShutdown() && clawPool.isTerminated()) {
                WebSocketServer.getInstance().broadcast("抢票停止");
                break;
            }
            try {
                Thread.sleep(2000);
                WebSocketServer.getInstance().broadcast("等待服务器响应停止，请勿操作...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        seatRefreshPool = null;
        workQueue.clear();
        stopSign.set(false);
    }
}
