import React, { useEffect, useState } from 'react';
import { Routes, Route} from 'react-router-dom';
import logo from './Logo.jpg';
import './App.css';
import Message from "./Message.js";

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

  // Api calls, these methods should be wrapped in a useEffect hook
  // Get the authenticated user's info
  async function getClaims() {
    var requestOptions = {
      method: 'GET',
      redirect: 'follow'
    }
    const claims = await fetch("http://localhost:9080/api/v10/@me/claims", requestOptions);
    return claims.json();
  }

  // Get the authenticated user's guilds
  async function getGuilds() {
    var requestOptions = {
      method: 'GET',
      redirect: 'follow'
    }
    const guilds = await fetch("http://localhost:9080/api/v10/@me/guilds", requestOptions);
    return guilds.json();
  }

  // Get all channels from a guild
  async function getChannels (guild_id) {
    var requestOptions = {
      method: 'GET',
      redirect: 'follow'
    }
    const channels = await fetch("http://localhost:9080/api/v10/guilds/" + guild_id + "/channels", requestOptions);
    return channels.json();
  }

  // Get all messages in a guild
  async function getAllMessages (guild_id) {
    var formdata = new FormData();
    formdata.append("Server_id", guild_id);
    var requestOptions = {
      method: 'POST',
      body: formdata,
      redirect: 'follow'
    };
    const messages = await fetch("http://localhost:9080/api/Discord/Msg", requestOptions);
    return messages.json();

  }

  // Get all messages in a guild made by a user
  async function getMessagesByAuthor (guild_id, author_id) {
    var formdata = new FormData();
    formdata.append("Server_id", guild_id);
    formdata.append("Discord_id", author_id)
    var requestOptions = {
      method: 'POST',
      body: formdata,
      redirect: 'follow'
    }
    const messages = await fetch("http://localhost:9080/api/Discord/MsgByAuthor", requestOptions);
    return messages.json();
  }

  // Get all messages from a channel in a guild
  async function getMessagesByChannel (guild_id, channel_id) {
    var formdata = new FormData();
    formdata.append("guild_id", guild_id);
    formdata.append("channel_id", channel_id)
    var requestOptions = {
      method: 'POST',
      body: formdata,
      redirect: 'follow'
    }
    const messages = await fetch("http://localhost:9080/api/Discord/msgs-in-channel", requestOptions);
    return messages.text();
  }
    
  useEffect(() => {
    getClaims()
      .then((res) => {setClaims(res);});
    getGuilds()
      .then((res) => {setGuilds(res); console.log("guilds"); console.log(res)});      
  },[]);

  useEffect(() => {
    if(guilds == null) return;
    getChannels(guilds[0].id)
      .then((res) => {console.log("channels");console.log(res)})
  },[guilds]);

  // Uncomment the function below to test getting messages, replace the server id and channel id with the ones from the server and
  // channel you are testing, if the bot is in a server the conole can be inspected to see the server and the channels in that server.
  // To update the server with changes to this file run the command: "npm run build" 
  // in this folder: DB_Engine_Networking\OpenLiberty\src\main\frontend>
  // If help is needed for testing ping jacob on discord for help
  /*
  useEffect(() => {
    getMessagesByChannel(server id, channel id)
      .then((res) => {setMessages(JSONbig.parse(res))});
  },[guilds]);
  */
 
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
          onClick={(e) => {console.log(guilds)}}> 
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
