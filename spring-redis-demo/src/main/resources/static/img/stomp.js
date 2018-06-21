var stompClient = null;
function connect() {
    var socket = new SockJS('/mysocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/own_messages', function(post) {
            //showGreeting(JSON.parse(postMessage.body).text);
            alert('New News that MAGA!');
        });
    });
}
function sendMessage() {
    var message = document.getElementById('message').value;
    stompClient.send("/messages/addmessage", {}, JSON.stringify({'message' : text}));
}
function setConnected(connected) {
    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
    document.getElementById('conversationDiv').style.visibility = connected ? 'visible' : 'hidden';
    document.getElementById('response').innerHTML = '';
}

function disconnect() {
    if (stompClient != null)
        stompClient.disconnect();
    setConnected(false);
}