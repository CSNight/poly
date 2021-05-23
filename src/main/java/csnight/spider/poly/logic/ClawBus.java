package csnight.spider.poly.logic;

import csnight.spider.poly.model.SeatInfo;
import csnight.spider.poly.model.ShowDetail;
import csnight.spider.poly.utils.ReflectUtils;
import csnight.spider.poly.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
    private final ScheduledExecutorService seatRefreshPool = Executors.newScheduledThreadPool(2);
    private final ScheduledExecutorService clawPool = Executors.newScheduledThreadPool(2);

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
            _log.warn("old request block");
            return;
        }
        if (seats == null) {
            _log.error("seat request failed");
            return;
        }
        if (seats.size() == 0) {
            WebSocketServer.getInstance().broadcast("票暂时售罄，继续刷新中... \n如需终止请点击停止按钮");
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

    public void StartClaw() {
        seatRefreshPool.scheduleAtFixedRate(this::GetClassSeat, 0, 5000, TimeUnit.MILLISECONDS);
        seatRefreshPool.scheduleAtFixedRate(this::GetClassSeat, 500, 5000, TimeUnit.MILLISECONDS);
    }
}
