let ws = null;
let url = "ws://localhost:8080/ws";

function setConnected(connected) {
    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
    document.getElementById('echo').disabled = !connected;
}

function connect() {
    ws = new WebSocket(url);
    ws.onopen = function () {
        setConnected(true);
        log('Info: Connection Established.');
    };

    ws.onmessage = function (event) {
        log('[receive] - '+ event.data);
    };

    ws.onclose = function (event) {
        setConnected(false);
        log('Info: Closing Connection.');
    };
}

function disconnect() {
    if (ws != null) {
        ws.close();
        ws = null;
    }
    setConnected(false);
}

function echo() {
    if (ws != null) {
        let message = document.getElementById('message').value;
        let msg = JSON.stringify({type: 'msg', message: message})
        log('[sent] - ' + msg);
        ws.send(msg);
    } else {
        alert('connection not established, please connect.');
    }
}

function log(message) {
    let console = document.getElementById('logging');
    let p = document.createElement('p');
    p.appendChild(document.createTextNode(message));
    console.appendChild(p);
}