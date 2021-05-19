package csnight.spider.poly.websocket;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;


public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        HttpResponseStatus responseStatus = res.status();
        if (responseStatus.code() != 200) {
            ByteBufUtil.writeUtf8(res.content(), responseStatus.toString());
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
        // Send the response and close the connection if necessary.
        boolean keepAlive = HttpUtil.isKeepAlive(req) && responseStatus.code() == 200;
        HttpUtil.setKeepAlive(res, keepAlive);
        ChannelFuture future = ctx.write(res); // Flushed in channelReadComplete()
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object Frame) {
        if (Frame instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) Frame);
        } else if (Frame instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) Frame);
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        WebSocketServer.getInstance().getChannelGroup().remove(ctx.channel());
        System.out.println("Remote Client disconnected!");
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
            String clientIP = inSocket.getAddress().getHostAddress();
            logger.info("Client from " + clientIP + " connect");
            ctx.channel().writeAndFlush(new TextWebSocketFrame("connected"));
            WebSocketServer.getInstance().getChannelGroup().add(ctx.channel());
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // 如果HTTP解码失败，返回HTTP异常
        if (!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }//获取url后置参数
        String uri = req.uri();
        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req, uri), null, false);
        WebSocketServerHandshaker hand_shaker = wsFactory.newHandshaker(req);
        if (hand_shaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            hand_shaker.handshake(ctx.channel(), req);
        }
    }

    private String getWebSocketLocation(FullHttpRequest req, String uri) {
        return "ws://" + req.headers().get(HttpHeaderNames.HOST) + uri;
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            String clientMsg = ((TextWebSocketFrame) frame).text();
            try {
                JSONObject msg = JSONObject.parseObject(clientMsg);

            } catch (Exception ex) {

            }
        } else if (frame instanceof BinaryWebSocketFrame) {
            logger.info("Binary msg received");
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.isFinalFragment(), frame.rsv(), frame.copy().content()));
        } else if (frame instanceof PingWebSocketFrame) {
            logger.info("Ping msg received");
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.isFinalFragment(), frame.rsv(), frame.copy().content()));
        } else if (frame instanceof CloseWebSocketFrame) {
            ctx.channel().close();
            logger.info("Close msg received");
        } else if (frame instanceof PongWebSocketFrame) {
            logger.info("Pong msg received");
            ctx.channel().writeAndFlush(new PingWebSocketFrame(frame.isFinalFragment(), frame.rsv(), frame.copy().content()));
        } else {
            logger.warn(String.format("%s frame types not supported", frame.getClass().getName()));
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        logger.warn(cause.getMessage());
    }


}
