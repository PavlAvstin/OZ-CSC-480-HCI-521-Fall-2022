import React, { createContext, Component, useEffect, useState } from 'react';
import { Routes, Route, Link, Navigate } from 'react-router-dom';
import logo from './Logo.jpg';
import './App.css';

  var User = null;

  const ProtectedRoute = ({user, children}) => {
    if(!User) {
      return <Navigate to="/signIn" replace />;
    }
    return children;
  }


  const App = () => {
    return (
      <>
        <Routes>
          <Route index element={<SignIn />} />
          <Route path="signIn" element={<SignIn />} />
          <Route path="discord/auth" element={<DiscordAuth />} />
          <Route path="messages" element={
            <ProtectedRoute >
              <Messages />
            </ProtectedRoute>
          } />
          <Route path="*" element={<p>There's nothing here: 404!</p>} />
        </Routes>
      </>

    );
  };

  const SignIn = () => {
    return (
      <div className='App SignIn'>
        <div className="body-content">
        <img src={logo} className="logo" alt="logo"/>
        <div className="description">Save and view important messages on Discord</div>
        <div className="login-text">Log in with Discord</div>
        <button id="login" className="clickable"
          onClick={(e) => {
            window.location.href="https://discord.com/api/oauth2/authorize?client_id=1031751361014022265&redirect_uri=http%3A%2F%2Flocalhost%3A3000%2Fdiscord%2Fauth&response_type=token&scope=identify"
          }}
          > Log In</button>
        </div>
      </div>
    );
  };

  const DiscordAuth = () => {
    const fragment = new URLSearchParams(window.location.hash.slice(1));
    const [accessToken, tokenType] = [fragment.get('access_token'), fragment.get('token_type')];
    const [state, setState] = useState(false);
    useEffect(() => {
      const fetchUserInfo = async () => {
        const response = await fetch('https://discord.com/api/users/@me', {
          headers: {
          authorization: `${tokenType} ${accessToken}`,
          },
        })
        const responseJson = await response.json();
        const { username, discriminator, avatar, id} = responseJson;
        User = {
          username: username + "#" + discriminator,
          avatar: "https://cdn.discordapp.com/avatars/" + id + "/" + avatar + ".jpg"
        };
        setState(true);
      };
      fetchUserInfo();
    }, []);
    if(state) {
      console.log(User);
      return <Navigate to="/messages" replace />;
    }
  }
  const Messages = () => {
    const [state, setState] = useState(false);
    if(User === null) {
      return <Navigate to="/signIn" replace />;
    }
    else {
      if(!state)
        setState(true);
      return (
        <div className='App Messages'>
          <header className='app-header'>
            <button className="home-button clickable">
              <img src={logo} className="home-logo" alt="logo"/>
            </button>
            <button className="logout-button clickable"
              onClick={(e) => { User = null; setState(false);}}> 
            Logout</button>
          </header> 
          <div className='side-bar'></div>
        </div>
      );
    }
  };
export default App;
