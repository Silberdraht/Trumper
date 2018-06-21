var stompClient = null;
$(document).ready(function () {

    connect();

    function connect() {
        var socket = new SockJS('/mysocket');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            console.log('Subscribed to: ' + '/newMessages/' + document.getElementById("currentUser").innerHTML)
            stompClient.subscribe('/newMessages/' + document.getElementById("currentUser").innerHTML, function (receiveMessage) {
                //sendMessage(); not used right now
                alert('New News that MAGA!');
                console.log('alert');
                notify()
            });
        });
    }

function notify() {
    if (!("Notification" in window)) {
        alert("This browser does not support desktop notification");
    }

    // Let's check whether notification permissions have alredy been granted
    else if (Notification.permission === "granted") {
        // If it's okay let's create a notification
        document.title = "New News!"
        var notification = new Notification("New News that MAGA!");
    }

    // Otherwise, we need to ask the user for permission
    else if (Notification.permission !== 'denied') {
        Notification.requestPermission(function (permission) {
            // If the user accepts, let's create a notification
            if (permission === "granted") {
                document.title = "New News!"
                var notification = new Notification("New News that MAGA!");
            }
        });
    }
}
})