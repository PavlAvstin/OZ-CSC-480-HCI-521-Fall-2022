import React, { useEffect, useState, useRef } from 'react';
import { Routes, Route} from 'react-router-dom';
import logo from './Logo.jpg';
import './App.css';
import Message from "./Message.js";
import SideBar from "./SideBar.js";
import ShareDialog from './ShareDialog';
import SearchBar from './SearchBar';

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
    location.href = PUBLIC_URL + "/messages";
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
  const [filters, setFilters] = useState({channels: [], reactions: [], users: [], search: ""});
  const [token, setToken] = useState("");
  const [sortedMessages, setSortedMessages] = useState(null);
  const [sortOldToNew, setSortOldToNew] = useState(false);
  const [users, setUsers] = useState(null);
  const [open, setOpen] = useState(false);
  const [shareId, setShareId] = useState(null);
  const filterRef = useRef();
  filterRef.current = filters;

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
    return allMessages;
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
    const dictionary = await fetch(API_URL + "/api/discord/Dictionary", requestOptions);
    if(dictionary.status == 401) {
      setToken(await getJWT()), () => {return getDictionary(guild_id);}
      return;
    }
    return dictionary.json();
  }
 
  // Get all of the users in the guild
  async function getUsers(guild_id) {
    var requestOptions = {
      method: 'GET',
      headers: {Authorization: "Bearer " + token},
      redirect: 'follow'
    }
    const users = await fetch(API_URL + "/api/v10/guilds/" + guild_id + "/members", requestOptions);
    if(users.status == 401) {
      setToken(await getJWT()), () => {return getUsers(guild_id);}
      return;
    }
    return users.text();
  }

  // Create a dm channel from the bot to a specified user
  async function createChannel(user_id) {
    var formdata = new FormData();
    formdata.append("recipientId", user_id);
    var requestOptions = {
      method: 'POST',
      headers: {Authorization: "Bearer " + token},
      body: formdata,
      redirect: 'follow'
    }
    const channel = await fetch(API_URL + "/api/v10/channels", requestOptions);
    if(channel.status == 401) {
      setToken(await getJWT()), () => {return createChannel(user_id);}
      return;
    }
    return channel.text()
  }

  // Create a dm channel with a user and send the message
  async function share(user_id, message_id) {
    const channel = JSONbig.parse(await createChannel(user_id));
    var formdata = new FormData();
    formdata.append("guildId", selectedGuild);
    formdata.append("messageId", message_id);
    var requestOptions = {
      method: 'POST',
      headers: {Authorization: "Bearer " + token},
      body: formdata,
      redirect: 'follow'
    }
    const request = await fetch(API_URL + "/api/v10/channels/" + channel.id + "/messages", requestOptions);
    if(request.status == 401) {
      setToken(await getJWT()), () => {return share(user_id, message_id);}
      return;
    }
    return;
  }

  // Logout of the page and redirect to the login screen
  async function logout() {
    var requestOptions = {
      method: 'GET',
      redirect: 'follow'
    }
    const request = await fetch(PUBLIC_URL + "/ibm_security_logout", requestOptions);
    location.href = PUBLIC_URL;
  }
  // Return true if the given message was reacted to by the given user
  function reactedBy(message, user_id) {
    var reactions = Array.from(message.reactions);
    for(const reaction of reactions) {
      if(reaction.author.discord_id.toString() === user_id.toString())
        return true;
    }
    return false;
  }

  // Check an array of messages to see if it contains a message
  function contains(messages, message) {
    if(messages.length == 0) return false;
    for(const msg of messages) {
      if(msg.discord_id.toString() == message.discord_id.toString())
        return true;
    }
    return false;
  }

  function filterSearch(messages) {
    if(filters.search == "") return messages;
    var newMessages = [];
    for(const message of messages) {
      if(message.content.toString().includes(filters.search))
        newMessages.push(message);
    }
    return newMessages;
  }

  // Custom poll hook
  function usePollingEffect(
    asyncCallback,
    dependencies = [],
    { 
      interval = 10_000, // 10 seconds,
      onCleanUp = () => {}
    } = {},
  ) {
    const timeoutIdRef = useRef(null)
    useEffect(() => {
      let _stopped = false
      ;(async function pollingCallback() {
        try {
          await asyncCallback()
        } finally {
          timeoutIdRef.current = !_stopped && setTimeout(
            pollingCallback,
            interval
          )
        }
      })()
      return () => {
        _stopped = true
        clearTimeout(timeoutIdRef.current)
        onCleanUp()
      }
    }, [...dependencies, interval])
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
    if(selectedGuild == null) return;
    const wrapper = async () => {
      var allMessages = [];
      var reactionMessages = [];
      var userMessages = [];
      if(filters.channels.length == 0) {
        allMessages = await getAllMessages(selectedGuild);
      }
      const getMsgsByChannel = async ()  => {
        for(const channel of Array.from(filters.channels)) {
          var messages = await getMessagesByChannel(selectedGuild, channel)
          messages = JSONbig.parse(messages);
          messages.forEach(message => allMessages.push(message))
        }
      }
      const getMsgsByReaction = async () => {
        for(const reaction of Array.from(filters.reactions)) {
          var messages = await getMessagesByReaction(selectedGuild, reaction)
          messages = JSONbig.parse(messages);
          messages.forEach(message => {
            if(allMessages.some(e => e.discord_id.toString() === message.discord_id.toString())) {
              if(!contains(reactionMessages, message))
                reactionMessages.push(message)
            }
          })
        }
      }
      const getMsgsByUser = async () => {
        for(const user of Array.from(filters.users)) {
          if(filters.reactions.length == 0) {
            var filtered = allMessages.filter(e => reactedBy(e, user));
            filtered.forEach(message => {
              if(!contains(userMessages, message))
                userMessages.push(message)
            })
          }
          else {
            filtered = reactionMessages.filter(e => reactedBy(e, user));
            filtered.forEach(message => {
              if(!contains(userMessages, message))
                userMessages.push(message)
            })
          }
        }
      }
      await getMsgsByChannel();
      await getMsgsByReaction();
      await getMsgsByUser();
      if(filters.users.length != 0)
        setMessages(filterSearch(userMessages))
      else if(filters.reactions.length != 0)
        setMessages(filterSearch(reactionMessages))
      else
        setMessages(filterSearch(allMessages))
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
    getUsers(selectedGuild)
      .then((res) => {setUsers(JSONbig.parse(res))});
  },[selectedGuild]);


  // When a guild is selected show all messages from it
  useEffect(() => {
    if(channels == null) return;
    getAllMessages(selectedGuild)
      .then((res) => {setMessages(res)})
  },[channels])

  // Sorts the messages by date reacted to
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
  },[messages, sortOldToNew])

  
  // Every 5 seconds poll to see if the db has changed
  usePollingEffect(
    async () => {setFilters({channels: filterRef.current.channels, reactions: filterRef.current.reactions, 
      users: filterRef.current.users, search: filterRef.current.search});},
    [],
    { interval: 5000 }
  )
  
  // Set the current guild, called from the sidebar
  function setGuildWrapper(guild_id) {
    setSelectedGuild(guild_id);
  }

  // Change which channels are selected, called from the sidebar
  function changeSelectedChannels(channel_id, checked) {
    if(!checked) {
      setFilters({channels: Array.from(filters.channels).filter(id => id != channel_id), reactions: filters.reactions, users: filters.users, search: filters.search});
      return
    }
    var newArr = Array.from(filters.channels);
    newArr.push(channel_id);
    setFilters({channels: newArr, reactions: filters.reactions, users: filters.users, search: filters.search})
  }

  // Change which reactions are selected, called from the sidebar
  function changeSelectedReactions(emoji, checked) {
    if(!checked) {
      setFilters({channels: filters.channels, reactions: Array.from(filters.reactions).filter(reaction => reaction != emoji), users: filters.users, search: filters.search})
      return
    }
    var newArr = Array.from(filters.reactions);
    newArr.push(emoji);
    setFilters({channels: filters.channels, reactions: newArr, users: filters.users, search: filters.search})
  }

  // Change which users are selected, called from the sidebar
  function changeSelectedUsers(user_id, checked) {
    if(!checked) {
      setFilters({channels: filters.channels, reactions: filters.reactions, users: Array.from(filters.users).filter(user => user != user_id), search: filters.search})
      return;
    }
    var newArr = Array.from(filters.users);
    newArr.push(user_id);
    setFilters({channels: filters.channels, reactions: filters.reactions, users: newArr, search: filters.search})
  }

  // Change the sort method, called from the sidebar
  function changeSortOrder(mode) {
    setSortOldToNew(mode);
  }

  // Create a message element for each message
  function messageList (messageArray) {
    if(messageArray == null) return;
    const list = messageArray.map((message) =>
      <li className="messageContainer"><Message message={message} filters={filters} dictionary={dictionary}  shareMenu={shareMenu}/></li>
    )
    return list;
  }

  // Open the share menu, called from a message
  function shareMenu(message_id) {
    setOpen(true);
    setShareId(message_id);
  }

  // Close the share menu, called from the share menu
  function handleClose(user_id) {
    setOpen(false);
    if(user_id == null) {setShareId(null); return};
    share(user_id, shareId);
    setShareId(null)
  }

  function handleSearch(value) {
    setFilters({channels: filters.channels, reactions: filters.reactions, users: filters.users, search: value})
  }

  return (
    <div className='App Messages'>
      <ShareDialog 
        onClose={handleClose}
        open={open}
        users={users}
      />
      <header className='app-header'>
        <button className="home-button">
          <img src={logo} className="home-logo" alt="logo"/>
        </button>
        <button className="logout-button clickable"
          onClick={(e) => {logout()}}> 
        Logout</button>
      </header> 
      <SideBar 
        guilds={guilds} 
        channels={channels} 
        users={users} 
        reactions={dictionary} 
        selectGuild={setGuildWrapper} 
        changeSelectedChannels={changeSelectedChannels}
        changeSelectedReactions={changeSelectedReactions}
        changeSelectedUsers={changeSelectedUsers}
        changeSortOrder={changeSortOrder}
      />
      <ul className='messagesContainer'>
        <SearchBar handleSearch={handleSearch}/>
        {messageList(sortedMessages)}
      </ul>
    </div>
    );
  }
export default App;