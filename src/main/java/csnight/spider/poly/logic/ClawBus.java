package csnight.spider.poly.logic;

import csnight.spider.poly.model.OrderInfo;
import csnight.spider.poly.model.SeatInfo;
import csnight.spider.poly.model.ShowDetail;
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
import java.util.concurrent.locks.ReentrantLock;

public class ClawBus {
    private static ClawBus ourInstance;
    private final Logger _log = LoggerFactory.getLogger(ClawBus.class);
    private ShowService service;
    private ShowDetail showDetail;
    private Map<String, List<SeatInfo>> seatWithClass = new ConcurrentHashMap<>();
    private AtomicLong timeSeatLock = new AtomicLong(0);
    private ReentrantLock lock = new ReentrantLock();
    private ScheduledExecutorService seatRefreshPool;
    private LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(10);
    private ThreadPoolExecutor clawPool;
    private final int RETRY_TIME = 2;
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
        OrderInfo order = new OrderInfo();
        order.setProjectId(showDetail.getProjectId());
        order.setShowId(showDetail.getShowId());
        order.setShowTime(showDetail.getShowTime());
        order.setGetTicketName(dto.getGetTickName());
        order.setWatchers(dto.getShowWatcher());
        order.setPayWay(dto.getPayWay());
        Future<String> stringCompleteFuture = clawPool.submit(() -> {
            return "";
        });
    }

    public void StartClaw(OrderDto dto) {
        seatRefreshPool = Executors.newScheduledThreadPool(2);
        clawPool = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, workQueue);
        seatRefreshPool.scheduleAtFixedRate(this::GetClassSeat, 0, 5000, TimeUnit.MILLISECONDS);
        seatRefreshPool.scheduleAtFixedRate(this::GetClassSeat, 1000, 3000, TimeUnit.MILLISECONDS);
        WebSocketServer.getInstance().broadcast("抢票启动成功");
    }

    public void StopClaw() {
        if (stopSign.get()) {
            WebSocketServer.getInstance().broadcast("等待停止中，请勿重复点击");
            return;
        }
        stopSign.set(true);
        seatRefreshPool.shutdown();
        clawPool.shutdown();
        WebSocketServer.getInstance().broadcast("抢票停止中...");
        while (true) {
            if (seatRefreshPool.isShutdown() && clawPool.isShutdown()) {
                WebSocketServer.getInstance().broadcast("抢票停止");
                break;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        seatRefreshPool = null;
        clawPool = null;
        workQueue.clear();
        stopSign.set(false);
    }
}
