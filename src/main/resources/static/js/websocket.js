function websocket() {
    return {
        channel: '',
        WS: null,
        eventMethods: [],
        isConnected: false,
        connect: function (url) {
            let thisCallback = this;
            //判断当前浏览器是否支持WebSocket
            if ('WebSocket' in window) {
                this.WS = new WebSocket(url);
            }
            //连接发生错误的回调方法
            this.WS.onerror = function () {
                thisCallback.isConnected = false;
                thisCallback.broadcast("wsErr", "Websocket连接错误");
            };
            //连接成功建立的回调方法
            this.WS.onopen = function () {
                thisCallback.isConnected = true;
                thisCallback.broadcast("wsOpen", "Websocket连接错误");
            };
            //接收到消息的回调方法
            this.WS.onmessage = function (event) {
                let data = event.data;
                thisCallback.broadcast("msg", data)
            };
            this.WS.onclose = function () {
                thisCallback.isConnected = false;
                thisCallback.broadcast("wsClose", "WebSocket连接关闭");
            };
            window.onbeforeunload = function () {
                thisCallback.close();
            };
        },
        send: function (request) {
            try {
                if (this.isConnected) {
                    this.WS.send(JSON.stringify(request));
                    return true
                }
            } catch (e) {
                console.error(e);
            }
            return false
        },
        close: function () {
            if (this.WS !== null) {
                this.WS.close(1000, "complete");
            }
        },
        async: function (args, handler) {
            setTimeout(() => {
                handler.method.apply(handler.method, [args])
            }, 10)
        },
        on: function (eventName, listener, id) {
            let delay = 0;
            let identifier = "";
            if (arguments.length === 3 && (arguments[2] === +arguments[2])) {
                delay = parseInt(arguments[2]);
                identifier = "";
            } else if (arguments.length === 3 && (arguments[2] === +arguments[2])) {
                identifier = id;
            } else if (arguments.length === 4) {
                delay = parseInt(arguments[3]);
                identifier = id;
            }
            this.eventMethods.push({
                identifier: identifier,
                eventName: eventName,
                method: listener,
                delay: delay
            });
            return identifier;
        },
        emit: function (eventName, data, evtId) {
            for (let index = 0; index < this.eventMethods.length; index++) {
                let handler = this.eventMethods[index];
                if (handler.eventName === eventName && evtId && handler.identifier === evtId) {
                    this.async(data, handler);
                    break;
                } else if (!evtId && handler.eventName === eventName) {
                    this.async(data, handler);
                }
            }
        },
        broadcast: function (eventName, data) {
            if (!this.eventMethods) this.eventMethods = [];
            for (let index = 0; index < this.eventMethods.length; index++) {
                let handler = this.eventMethods[index];
                if (handler.eventName === eventName) {
                    this.async(data, handler);
                }
            }
        },
        un: function (eventName, id) {
            for (let index = 0; index < this.eventMethods.length; index++) {
                let handler = this.eventMethods[index];
                if (id) {
                    if (handler.eventName === eventName && handler.identifier === id) {
                        this.eventMethods.splice(index, 1);
                        index--;
                    }
                } else {
                    if (handler.eventName === eventName) {
                        this.eventMethods.splice(index, 1);
                        index--;
                    }
                }
            }
        },
        clear: function () {
            this.eventMethods = [];
        },
        destroy: function () {
            this.eventMethods = [];
        }
    }
}
