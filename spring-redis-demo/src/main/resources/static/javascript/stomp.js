var stompClient = null;
$(document).ready(function () {

    connect();

    function connect() {
        var socket = new SockJS('/mysocket');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/newMessages/' + document.getElementById("currentUser").innerHTML, function (receiveMessage) {
                //sendMessage(); not used right now
                alert('New News that MAGA!');
                console.log('alert');
                //$('[data-toggle="popover"]').popover();
                //document.getElementById("newMsg").innerText = "<p>You have updated news.</p>";
                document.getElementById("newMsg").setAttribute(innerText, "You've got news!");
            });
        });
    }


    function sendMessage() {
        var message = document.getElementById('message').value;
        stompClient.send("/messages/addmessage", {}, JSON.stringify({'message': text}));
    }
})