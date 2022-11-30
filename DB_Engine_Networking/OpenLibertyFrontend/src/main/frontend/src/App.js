import React, { Component, useCallback } from 'react';
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
    const navigate = useNavigate();
    const handleOnClick = useCallback(() => navigate('/messages', {replace: true}), [navigate]);
    return (
      <div className='App SignIn'>
        <div className="body-content">
        <img src={logo} className="logo" alt="logo"/>
        <div className="description">Save and view important messages on Discord</div>
        <div className="login-text">Log in with Discord</div>
        <button id="login" className="clickable"
          onClick={
            handleOnClick
          }
          > Log In</button>
        </div>
      </div>
    );
  };

  const Messages = () => {
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
      </div>
    );
  }
export default App;
