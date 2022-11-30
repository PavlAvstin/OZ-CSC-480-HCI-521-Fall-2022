import React, { useEffect, useState } from 'react';
import { Routes, Route} from 'react-router-dom';
import logo from './Logo.jpg';
import './App.css';
import Message from "./Message.js";
import SideBar from "./SideBar.js";

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
  const [channels, setChannels] = useState(null);
  const [dictionary, setDictionary] = useState(null);
  const [selectedGuild, setSelectedGuild] = useState(null);
  const [filters, setFilters] = useState({channels: [], reactions: []});
  const [token, setToken] = useState("");
  const [sortedMessages, setSortedMessages] = useState(null);
  const [sortOldToNew, setSortOldToNew] = useState(false);

  // Api calls, these methods should be wrapped in a useEffect hook
  // Request a new jwt from the server
  async function getJWT() {
    var requestOptions = {
      method: 'GET',
      redirect: 'follow'      
    }
    const jwt = await fetch(PUBLIC_URL + "/api/jwt/issue");
    return jwt.text();
  }

  // Get the authenticated user's info
  async function getClaims() {
    var requestOptions = {
      headers: {Authorization: "Bearer " + token},
      method: 'GET',
      redirect: 'follow'
    }
    var claims = await fetch(API_URL + "/api/v10/@me/claims", requestOptions);
    if(claims.status == 401) {
      setToken(await getJWT()), () => { return getClaims(); }
      return;
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
      setToken(await getJWT()), () => { return getGuilds();}
      return;
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
      setToken(await getJWT()), () => {return getChannels(guild_id);}
      return;
    }
    return channels.json();
  }

  // Get all messages in a guild
  async function getAllMessages (guild_id) {
    var allMessages = [];
    const getMsgsByChannel = async ()  => {
      for(const element of Array.from(channels)) {
        var messages = await getMessagesByChannel(guild_id, element.id)
        messages = JSONbig.parse(messages);
        messages.forEach(message => allMessages.push(message))
      }
    }
    await getMsgsByChannel();
    setMessages(allMessages)
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
      setToken(await getJWT()), () => {return getMessagesByAuthor(guild_id, author_id);}
      return;
    }
    return messages.text();
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
      setToken(await getJWT()), () => {return getMessagesByChannel(guild_id, channel_id);}
      return;
    }
    return messages.text();
  }

  // Get all messages from a server with a certain reaction
  async function getMessagesByReaction(guild_id, reaction) {
    var formdata = new FormData();
    formdata.append("Server_id", guild_id);
    formdata.append("Reaction", reaction);
    var requestOptions = {
      method: 'POST',
      headers: {Authorization: "Bearer " + token},
      body: formdata,
      redirect: 'follow'
    }
    const messages = await fetch(API_URL + "/api/discord/MsgByReaction", requestOptions);
    if(messages.status == 401) {
      setToken(await getJWT()), () => {return getMessagesByReaction(guild_id, reaction);}
      return;
    }
    return messages.text();
  }

  // Get the dictionary for a guild
  async function getDictionary(guild_id) {
    var formdata = new FormData();
    formdata.append("Server_id", guild_id);
    var requestOptions = {
      method: 'POST',
      headers: {Authorization: "Bearer " + token},
      body: formdata,
      redirect: 'follow'
    }
    const messages = await fetch(API_URL + "/api/discord/Dictionary", requestOptions);
    if(messages.status == 401) {
      setToken(await getJWT()), () => {return getDictionary(guild_id);}
      return;
    }
    return messages.json();
  }
 
  // When the page loads get a jwt, this only runs once
  useEffect(() => {
    getJWT() 
      .then((res) => {setToken(res)})
  },[])
  
  // Get the user specific information
  useEffect(() => {
    if(token == "")
      return;
    getClaims()
      .then((res) => {setClaims(res)});
    getGuilds()
      .then((res) => {setGuilds(res);});      
  },[token]);


  // Whenever the filters are updated change the messages that are displayed
  useEffect(() => {
    const wrapper = async () => {
      var allMessages = [];
      const getMsgsByChannel = async ()  => {
        for(const element of Array.from(filters.channels)) {
          var messages = await getMessagesByChannel(selectedGuild, element)
          messages = JSONbig.parse(messages);
          messages.forEach(message => allMessages.push(message))
        }
      }
      const getMsgsByReaction = async () => {
        for(const element of Array.from(filters.reactions)) {
          var messages = await getMessagesByReaction(selectedGuild, element)
          messages = JSONbig.parse(messages);
          messages.forEach(message => {
            if(!allMessages.some(e => e.discord_id.toString() === message.discord_id.toString())) {
              allMessages.push(message)
            }
          })
        }
      }
      await getMsgsByChannel();
      await getMsgsByReaction();
      setMessages(allMessages)
    }
    wrapper();
  },[filters]);

  // When a user clicks a guild, display the filters for that guild
  useEffect(() => {
    if(selectedGuild == null) return;
    getDictionary(selectedGuild)
      .then((res) => {setDictionary(res)});
    getChannels(selectedGuild)
      .then((res) => {setChannels(res);})
  },[selectedGuild]);

  useEffect(() => {
    if(channels == null) return;
    getAllMessages(selectedGuild);
  },[channels])

  useEffect(() => {
    if(messages == null) return;
    var sortedMessages = Array.from(messages);
    if(sortOldToNew) 
      sortedMessages.sort(function(a,b) {
        return new Date(a.updated_at) - new Date(b.updated_at);
      })
    else
      sortedMessages.sort(function(a,b) {
        return new Date(b.updated_at) - new Date(a.updated_at);
      })
    setSortedMessages(sortedMessages);
  },[messages])

  // Set the current guild, called from the sidebar
  function setGuildWrapper(guild_id) {
    setSelectedGuild(guild_id);
  }

  // Change which channels are selected, called from the sidebar
  function changeSelectedChannels(channel_id, checked) {
    if(!checked) {
      setFilters({channels: Array.from(filters.channels).filter(id => id != channel_id), reactions: filters.reactions});
      return
    }
    var newArr = Array.from(filters.channels);
    newArr.push(channel_id);
    setFilters({channels: newArr, reactions: filters.reactions})
  }

  // Change which reactions are selected, called from the sidebar
  function changeSelectedReactions(emoji, checked) {
    if(!checked) {
      setFilters({channels: filters.channels, reactions: Array.from(filters.reactions).filter(reaction => reaction != emoji)})
      return
    }
    var newArr = Array.from(filters.reactions);
    newArr.push(emoji);
    setFilters({channels: filters.channels, reactions: newArr})
  }

  // Create a message element for each message
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
      <SideBar 
        guilds={guilds} 
        channels={channels} 
        users={null} 
        reactions={dictionary} 
        selectGuild={setGuildWrapper} 
        changeSelectedChannels={changeSelectedChannels}
        changeSelectedReactions={changeSelectedReactions}
      />
      <ul className='messagesContainer'>
        {messageList(sortedMessages)}
      </ul>
    </div>
    );
  }
export default App;
