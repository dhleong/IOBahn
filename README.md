IOBahn
======

[Socket.IO protocol](https://github.com/LearnBoost/socket.io-spec) implementation on top of [AutobahnAndroid](https://github.com/tavendo/AutobahnAndroid) WebSockets library.
This works with the lattest Socket.IO protocol.

Many thanks to the [Socket.IO](http://socket.io/) and [Autobahn](http://autobahn.ws/) projects.
Also thanks to the [android-websockets](https://github.com/koush/android-websockets) fellas, I got some ideas from their code.

## Why?

Because we like to develop servers with Socket.IO, and clients on Android.
And also because Autobahn provide a robust library for the transport layer, the websockets.

## Overview

It works a lot like the Autobahn library for the WAMP protocol; but this extension implements the Socket.IO protocol.

## How to use it? 

First instantiate a Socket.IO connection:
```java
private final SocketIO mConnection = new SocketIOConnection();
```

Then initiate the connection, implementing the handler:
```java
mConnection.connect(wsuri, new SocketIO.ConnectionHandler() {
  public void onOpen() {
  //What to do on WebSocket openning
  }
  
  public void onClose(int code, String reason) {
   //What to do on WebSocket closing
  }
});
```
If you want to know for sure why the connection closed, and handle connection related problems, you only need to take a look at the code and reason that the onClose handler pass to you.

Subscribe to the desired events, declaring their handlers and the types of the incomming objects:
```java
// Interface: SocketIO.on(String name, Class<?> eventType, EventHandler eventHandler)
mConnection.on("myevent", MyEvent.class, new SocketIO.EventHandler() {
  public void onEvent(Object event){
    MyEvent myevent = (MyEvent) event; // we can safely cast to the class we specified before
    //What to do on event happening
  }
});
```

If you wan to emit an event, simply call emit:
```java
// Interface: SocketIO.emit(String name, Object event)
mConnection.emit("myevent", new MyEvent("foo"));
```

Finally if you want to disconnect, call disconnect:
```java
// Interface: SocketIO.disconnect()
mConnection.disconnect();
```

Optionally disconnect only from an endpoint:
```java
// Interface: SocketIO.disconnect(String endpoint)
mConnection.disconnect("testendpoint");
```

##Demo
Go to [IOBahn.SimpleEventsDemo](https://github.com/magnux/IOBahn.SimpleEventsDemo) repo, there you can download it and see IOBahn in action.

##Downloads
Library packs are available under the downloads section. Download and extract them in your libs/ directory, and you're ready to start using IOBahn.

##License
(The MIT License)
    
    Copyright (c) 2012 Alejandro Hernandez
    
    Permission is hereby granted, free of charge, to any person obtaining a copy of
    this software and associated documentation files (the 'Software'), to deal in
    the Software without restriction, including without limitation the rights to use,
    copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
    Software, and to permit persons to whom the Software is furnished to do so,
    subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
    FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
    COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
    IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.