/**
 * ============================================================
 *  Chat App - Client Side JavaScript
 *  Socket.IO Real-time Communication
 * ============================================================
 */

// ============================================================
// GLOBAL VARIABLES
// ============================================================

const socket = io();

let currentUser = null;
let currentRoom = null;
let isTyping = false;
let typingTimeout;

const typingUsers = new Set();

// ============================================================
// DOM ELEMENTS
// ============================================================

const loginContainer = document.getElementById('loginContainer');
const chatContainer = document.getElementById('chatContainer');

const userNameInput = document.getElementById('userNameInput');
const roomNameInput = document.getElementById('roomNameInput');
const loginBtn = document.getElementById('loginBtn');

const roomInput = document.getElementById('roomInput');
const joinRoomBtn = document.getElementById('joinRoomBtn');
const leaveRoomBtn = document.getElementById('leaveRoomBtn');

const messageInput = document.getElementById('messageInput');
const sendBtn = document.getElementById('sendBtn');
const messagesContainer = document.getElementById('messagesContainer');

const roomTitle = document.getElementById('roomTitle');
const currentRoomDisplay = document.getElementById('currentRoom');
const userCountBadge = document.getElementById('userCountBadge');
const usersList = document.getElementById('usersList');
const userListSidebar = document.getElementById('userListSidebar');
const roomsList = document.getElementById('roomsList');
const roomInfo = document.getElementById('roomInfo');

const typingIndicator = document.getElementById('typingIndicator');
const typingUser = document.getElementById('typingUser');

const connectionStatus = document.getElementById('connectionStatus');

// ============================================================
// SOCKET.IO EVENTS
// ============================================================

/**
 * Connection Success
 */
socket.on('connect', () => {
    console.log('Connected to server');
    connectionStatus.classList.remove('status-disconnected');
    connectionStatus.classList.add('status-connected');
});

/**
 * Connection Lost
 */
socket.on('disconnect', () => {
    console.log('Disconnected from server');
    connectionStatus.classList.remove('status-connected');
    connectionStatus.classList.add('status-disconnected');
});

/**
 * Room Data Received - User berhasil join room
 */
socket.on('room-data', (data) => {
    console.log('Received room data:', data);
    
    currentRoom = data.roomName;
    
    // Update UI
    chatContainer.style.display = 'flex';
    roomTitle.textContent = data.roomName;
    currentRoomDisplay.textContent = data.roomName;
    roomInfo.style.display = 'block';
    userListSidebar.style.display = 'block';
    
    messageInput.disabled = false;
    sendBtn.disabled = false;
    
    // Update users list
    updateUsersList(data.users);
    userCountBadge.textContent = data.userCount;
    
    // Clear messages dan tampilkan history
    messagesContainer.innerHTML = '';
    data.messages.forEach(msg => {
        displayMessage(msg);
    });
    
    // Scroll ke bawah
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
});

/**
 * User Joined Room
 */
socket.on('user-joined', (data) => {
    console.log('User joined:', data);
    
    // Display system message
    addSystemMessage(data.message, 'info');
    
    // Update user count
    userCountBadge.textContent = data.userCount;
});

/**
 * User Left Room
 */
socket.on('user-left', (data) => {
    console.log('User left:', data);
    
    addSystemMessage(data.message, 'warning');
    userCountBadge.textContent = data.userCount;
});

/**
 * Receive Message
 */
socket.on('receive-message', (message) => {
    console.log('Message received:', message);
    displayMessage(message);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
});

/**
 * User Typing
 */
socket.on('user-typing', (data) => {
    typingUsers.add(data.userName);
    updateTypingIndicator();
});

/**
 * User Stop Typing
 */
socket.on('user-stop-typing', (data) => {
    typingUsers.delete(data.userName);
    updateTypingIndicator();
});

/**
 * Rooms List
 */
socket.on('rooms-list', (roomsList_data) => {
    console.log('Rooms received:', roomsList_data);
    
    const list = document.getElementById('roomsList');
    list.innerHTML = '';
    
    if (roomsList_data.length === 0) {
        list.innerHTML = '<li style="color: #7f8c8d; font-style: italic;">No rooms available</li>';
        return;
    }
    
    roomsList_data.forEach(room => {
        const li = document.createElement('li');
        li.textContent = `${room.name} (${room.userCount} users)`;
        li.onclick = () => quickJoinRoom(room.name);
        list.appendChild(li);
    });
});

// ============================================================
// LOGIN EVENTS
// ============================================================

loginBtn.addEventListener('click', () => {
    const userName = userNameInput.value.trim();
    const roomName = roomNameInput.value.trim();
    
    if (!userName) {
        alert('Please enter your name');
        userNameInput.focus();
        return;
    }
    
    if (!roomName) {
        alert('Please enter a room name');
        roomNameInput.focus();
        return;
    }
    
    // Emit join-room event
    socket.emit('join-room', {
        userName: userName,
        roomName: roomName
    });
    
    currentUser = userName;
    
    // Hide login, show chat
    loginContainer.style.display = 'none';
    chatContainer.style.display = 'flex';
});

// Allow Enter key to login
userNameInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') roomNameInput.focus();
});

roomNameInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') loginBtn.click();
});

// ============================================================
// CHAT EVENTS
// ============================================================

/**
 * Send Message
 */
sendBtn.addEventListener('click', sendMessage);

messageInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
    }
});

/**
 * Typing Indicator
 */
messageInput.addEventListener('input', () => {
    if (!isTyping) {
        isTyping = true;
        socket.emit('typing');
    }
    
    clearTimeout(typingTimeout);
    typingTimeout = setTimeout(() => {
        isTyping = false;
        socket.emit('stop-typing');
    }, 2000);
});

/**
 * Leave Room
 */
leaveRoomBtn.addEventListener('click', () => {
    socket.emit('leave-room');
    
    // Reset UI
    chatContainer.style.display = 'none';
    loginContainer.style.display = 'flex';
    messagesContainer.innerHTML = '';
    usersList.innerHTML = '<li class="no-users">No users yet</li>';
    userListSidebar.style.display = 'none';
    
    userNameInput.value = '';
    roomNameInput.value = '';
    messageInput.value = '';
    messageInput.disabled = true;
    sendBtn.disabled = true;
    
    currentUser = null;
    currentRoom = null;
    userNameInput.focus();
});

/**
 * Join Room from Input
 */
joinRoomBtn.addEventListener('click', () => {
    const roomName = roomInput.value.trim();
    if (!roomName) {
        alert('Please enter a room name');
        return;
    }
    
    if (!currentUser) {
        alert('Please login first');
        return;
    }
    
    // Leave current room if in one
    if (currentRoom) {
        socket.emit('leave-room');
    }
    
    // Join new room
    socket.emit('join-room', {
        userName: currentUser,
        roomName: roomName
    });
    
    roomInput.value = '';
});

// ============================================================
// HELPER FUNCTIONS
// ============================================================

/**
 * Send Message
 */
function sendMessage() {
    const text = messageInput.value.trim();
    
    if (!text) return;
    
    socket.emit('send-message', {
        text: text
    });
    
    messageInput.value = '';
    isTyping = false;
    socket.emit('stop-typing');
}

/**
 * Display Message in Chat
 */
function displayMessage(message) {
    const messageGroup = document.createElement('div');
    messageGroup.classList.add('message-group');
    
    const isOwn = message.userName === currentUser;
    if (isOwn) {
        messageGroup.classList.add('own');
    } else {
        messageGroup.classList.add('other');
    }
    
    const messageEl = document.createElement('div');
    messageEl.classList.add('message');
    messageEl.textContent = message.text;
    
    messageGroup.appendChild(messageEl);
    
    // Info (username and time)
    const infoEl = document.createElement('div');
    infoEl.classList.add('message-info');
    
    if (!isOwn) {
        infoEl.textContent = `${message.userName} • ${formatTime(message.timestamp)}`;
    } else {
        infoEl.textContent = formatTime(message.timestamp);
    }
    
    messageGroup.appendChild(infoEl);
    
    messagesContainer.appendChild(messageGroup);
}

/**
 * Add System Message
 */
function addSystemMessage(message, type = 'info') {
    const systemMsg = document.createElement('div');
    systemMsg.classList.add('system-message');
    systemMsg.textContent = message;
    messagesContainer.appendChild(systemMsg);
}

/**
 * Update Users List
 */
function updateUsersList(users) {
    usersList.innerHTML = '';
    
    if (users.length === 0) {
        usersList.innerHTML = '<li class="no-users">No users yet</li>';
        return;
    }
    
    users.forEach(user => {
        const li = document.createElement('li');
        const isCurrentUser = user.name === currentUser ? ' (You)' : '';
        li.textContent = user.name + isCurrentUser;
        usersList.appendChild(li);
    });
}

/**
 * Update Typing Indicator
 */
function updateTypingIndicator() {
    if (typingUsers.size === 0) {
        typingIndicator.style.display = 'none';
    } else {
        const names = Array.from(typingUsers).join(', ');
        typingUser.textContent = names;
        typingIndicator.style.display = 'block';
    }
}

/**
 * Quick Join Room from List
 */
function quickJoinRoom(roomName) {
    roomInput.value = roomName;
    joinRoomBtn.click();
}

/**
 * Format Timestamp
 */
function formatTime(timestamp) {
    const date = new Date(timestamp);
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
}

/**
 * Get Rooms List
 */
function refreshRoomsList() {
    socket.emit('get-rooms');
}

// Auto-refresh rooms list every 5 seconds
setInterval(refreshRoomsList, 5000);

// Initial focus
userNameInput.focus();

console.log('Chat App Client Loaded');
