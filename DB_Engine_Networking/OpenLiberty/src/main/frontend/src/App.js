import React, { useEffect, useState } from 'react';
import { Routes, Route, useNavigate } from 'react-router-dom';
import logo from './Logo.jpg';
import './App.css';

const App = () => {
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

const SignIn = () => {
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

const Messages = () => {
  // State variables, update with the set... method
  // When updated the component will be re-rendered 
  const [messages, setMessages] = useState([])
  const [claims, setClaims] = useState([]);
  const [guilds, setGuilds] = useState([]);

  // Api calls, these methods should be wrapped in a useEffect hook
  // Get the authenticated user's info
  const getClaims = async () => {
    requestOptions = {
      method: 'GET',
      redirect: 'follow'
    }
    const claims = await fetch("http://localhost:9080/api/v10/@me/claims", requestOptions);
    return claims.json();
  }

  // Get the authenticated user's guilds
  const getGuilds = async () => {
    requestOptions = {
      method: 'GET',
      redirect: 'follow'
    }
    const guilds = await fetch("http://localhost:9080/api/v10/@me/guilds", requestOptions);
    return guilds.json();
  }

  // Get all channels from a guild
  const getChannels = async (guild_id) => {
    requestOptions = {
      method: 'GET',
      redirect: 'follow'
    }
    const channels = await fetch("http://localhost:9080/api/v10/guilds/" + guild_id + "/channels", requestOptions);
    return channels.json();
  }

  // Get all messages in a guild
  const getAllMessages = async (guild_id) =>
  {
    var formdata = new FormData();
    formdata.append("Server_id", guild_id);
    var requestOptions = {
      method: 'POST',
      body: formdata,
      redirect: 'follow'
    };
    const messages = await fetch("http://localhost:9080/api/Discord/Msg", requestOptions);
    return messages;

  }

  // Get all messages in a guild made by a user
  const getMessagesByAuthor = async (guild_id, author_id) => {
    var formdata = new FormData();
    formdata.append("Server_id", guild_id);
    formdata.append("Discord_id")
    var requestOptions = {
      method: 'POST',
      body: formdata.append,
      redirect: 'follow'
    }
    const messages = await fetch("http://localhost:9080/api/Discord/MsgByAuthor", requestOptions);
    return messages;
  }

  // Get all messages from a channel in a guild
  const getMessagesByChannel = async (guild_id, channel_id) => {
    var formdata = new FormData();
    formdata.append("guild_id", guild_id);
    formdata.append("channel_id", channel_id)
    var requestOptions = {
      method: 'POST',
      body: formdata.append,
      redirect: 'follow'
    }
    const messages = await fetch("http://localhost:9080/api/Discord/msgs-in-channel", requestOptions);
    return messages;
  }
    
  useEffect(() => {
    getClaims()
      .then((res) => {setClaims(res)});
    getGuilds()
      .then((res) => {setGuilds(res)});      
  },[]);

  // A method similar to this can be used to display all messages or to populate the left side bar
  const guildNames = (servers) => {
    const names = servers.map((server) =>
      <li>{server.name}</li>
    )
    return names;
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
      <div className='messagesContainer'>

      </div>
    </div>
    );
  }
export default App;
