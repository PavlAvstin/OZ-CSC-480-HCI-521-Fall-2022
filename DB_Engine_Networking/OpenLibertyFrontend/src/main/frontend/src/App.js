import React, { useEffect, useState } from 'react';
import { Routes, Route} from 'react-router-dom';
import logo from './Logo.jpg';
import './App.css';
import Message from "./Message.js";

var API_URL = process.env.REACT_APP_API_URL;
var PUBLIC_URL = process.env.PUBLIC_URL;
var JSONbig = require('json-bigint');

function App() {
  return (
    <>
    <Routes>
      <Route index element={<SignIn />} />
      <Route path="signIn" element={<SignIn />} />
      <Route path="messages" element={ <Messages />} />
      <Route path="*" element={<p>There's nothing here: 404!</p>} />
    </Routes>
    </>

  );
};

function SignIn() {
  function redirectToDashboard(event) {
    event.preventDefault();
    location.href = "http://localhost:9080/messages";
  }
  return (
    <div className='App SignIn'>
      <div className="body-content">
        <img src={logo} className="logo" alt="logo"/>
        <div className="description">Save and view important messages on Discord</div>
        <div className="login-text">Log in with Discord</div>
        <button id="login" className="clickable"
          onClick={redirectToDashboard}
        >Log In</button>
      </div>
    </div>
  );
};

function Messages() {

  // State variables, update with the set... method
  // When updated the component will be re-rendered 
  const [messages, setMessages] = useState(null)
  const [claims, setClaims] = useState(null);
  const [guilds, setGuilds] = useState(null);
  var token = "";

  async function getJWT() {
    var requestOptions = {
      method: 'GET',
      redirect: 'follow'      
    }
    const jwt = await fetch(PUBLIC_URL + "/api/jwt/issue");
    return jwt.text();
  }
  // Api calls, these methods should be wrapped in a useEffect hook
  // Get the authenticated user's info
  async function getClaims() {
    var requestOptions = {
      headers: {Authorization: "Bearer " + token},
      method: 'GET',
      redirect: 'follow'
    }
    var claims = await fetch(API_URL + "/api/v10/@me/claims", requestOptions);
    if(claims.status == 401) {
      token = await getJWT();
      return getClaims();
    }
    return claims.json();
  }
  // Get the authenticated user's guilds
  async function getGuilds() {
    var requestOptions = {
      headers: {Authorization: "Bearer " + token},
      method: 'GET',
      redirect: 'follow'
    }
    const guilds = await fetch(API_URL + "/api/v10/@me/guilds", requestOptions);
    if(guilds.status == 401) {
      token = await getJWT();
      return getGuilds();
    }
    return guilds.json();
  }

  // Get all channels from a guild
  async function getChannels (guild_id) {
    var requestOptions = {
      headers: {Authorization: "Bearer " + token},
      method: 'GET',
      redirect: 'follow'
    }
    const channels = await fetch(API_URL + "/api/v10/guilds/" + guild_id + "/channels", requestOptions);
    if(channels.status == 401) {
      token = await getJWT();
      return getChannels(guild_id);
    }
    return channels.json();
  }

  // Get all messages in a guild
  async function getAllMessages (guild_id, message_id) {
    var formdata = new FormData();
    formdata.append("Server_id", guild_id);
    formdata.append("Discord_id", message_id)
    var requestOptions = {
      method: 'POST',
      headers: {Authorization: "Bearer " + token},
      body: formdata,
      redirect: 'follow'
    };
    const messages = await fetch(API_URL + "/api/discord/Msg", requestOptions);
    if(messages.status == 401) {
      token = await getJWT();
      return getAllMessages(guild_id);
    }
    return messages.json();

  }

  // Get all messages in a guild made by a user
  async function getMessagesByAuthor (guild_id, author_id) {
    var formdata = new FormData();
    formdata.append("Server_id", guild_id);
    formdata.append("Discord_id", author_id)
    var requestOptions = {
      method: 'POST',
      headers: {Authorization: "Bearer " + token},
      body: formdata,
      redirect: 'follow'
    }
    const messages = await fetch(API_URL + "/api/discord/MsgByAuthor", requestOptions);
    if(messages.status == 401) {
      token = await getJWT();
      return getMessagesByAuthor(guild_id, author_id);
    }
    return messages.json();
  }

  // Get all messages from a channel in a guild
  async function getMessagesByChannel (guild_id, channel_id) {
    var formdata = new FormData();
    formdata.append("guild_id", guild_id);
    formdata.append("channel_id", channel_id)
    var requestOptions = {
      method: 'POST',
      headers: {Authorization: "Bearer " + token},
      body: formdata,
      redirect: 'follow'
    }
    const messages = await fetch(API_URL + "/api/discord/msgs-in-channel", requestOptions);
    if(messages.status == 401) {
      token = await getJWT();
      return getMessagesByChannel(guild_id, channel_id);
    }
    return messages.text();
  }
    
  useEffect(() => {
    getClaims()
      .then((res) => {setClaims(res)});
    getGuilds()
      .then((res) => {setGuilds(res);});      
  },[]);

  useEffect(() => {
    if(guilds == null) return;
    getChannels(guilds[0].id)
      .then((res) => {})
  },[guilds]);
  
  useEffect(() => {
    getMessagesByAuthor("1034182952885694534","542537230762377217")
      .then((res) => {setMessages(JSONbig.parse(res)); console.log(JSONbig.parse(res))});
  },[guilds]);
  
 
  // A method similar to this can be used to display all messages or to populate the left side barm
  function messageList (messageArray) {
    if(messageArray == null) return;
    const list = messageArray.map((message) =>
      <li className="messageContainer"><Message message={message}/></li>
    )
    return list;
  }
  
  return (
    <div className='App Messages'>
      <header className='app-header'>
        <button className="home-button clickable">
          <img src={logo} className="home-logo" alt="logo"/>
        </button>
        <button className="logout-button clickable"
          onClick={(e) => {}}> 
        Logout</button>
      </header> 
      <div className='side-bar'></div>
      <ul className='messagesContainer'>
        {messageList(messages)}
      </ul>
    </div>
    );
  }
export default App;
