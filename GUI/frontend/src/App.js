import React, { Component, useCallback } from 'react';
import { Routes, Route, useNavigate } from 'react-router-dom';
import logo from './Logo.jpg';
import './App.css';
import Box from '@mui/material/Box';
import Container from '@mui/material/Container';
import Divider from '@mui/material/Divider';
import TextField from '@mui/material/TextField';
import InputBase from '@mui/material/InputBase';
import Paper from '@mui/material/Paper';
import IconButton from '@mui/material/IconButton';
import MenuIcon from '@mui/icons-material/Menu';
import SearchIcon from '@mui/icons-material/Search';
import DirectionsIcon from '@mui/icons-material/Directions';
import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import EmojiEmotionsIcon from '@mui/icons-material/EmojiEmotions';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import Checkbox from '@mui/material/Checkbox';
import FormLabel from '@mui/material/FormLabel';
import FormControl from '@mui/material/FormControl';
import FormGroup from '@mui/material/FormGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import Avatar from '@mui/material/Avatar';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import Typography from '@mui/material/Typography';
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';


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

 const label = { inputProps: { 'aria-label': 'Checkbox demo' } };

  const Messages = () => {
    return (
      <div className='App Messages'>
        <header className='app-header'>
          <button className="home-button clickable">
            <img src={logo} className="home-logo" alt="logo"/>
          </button>
            <Avatar className="avatar-icon"
              alt="Cam"
              src="/static/images/avatar/1.jpg"
              sx={{ width: 45, height: 45, margin: 4 }}
            />
            <button className="profile-name"
                onClick={(e) => {}}>
            Hi Cam </button>
            <button className="logout-button clickable"
                onClick={(e) => {}}>
            Logout</button>
        </header>
        <div className='side-bar'>
            <div className="side-bar-menus">
                <nav>
                    <label for="touch1"><span className="tag-style">All Servers & Channels
                        <ExpandMoreIcon />
                    </span></label>
                    <input type="checkbox" id="touch1" />
                    <li class="slide">
                        <List>
                            {['# General', '# Requirements', '# Usability', '# GUI', '# Quality-Assurance', '# DB-Engine-Networking' ].map((text, index) => (
                                <ListItem key={text} disablePadding>
                                    <ListItemButton>
                                        <ListItemText primary={text} />
                                    </ListItemButton>
                                </ListItem>
                            ))}
                        </List>
                    </li>
                </nav>
                <nav>
                    <label for="touch2"><span className="tag-style"><AccountCircleIcon />Users
                    <ExpandMoreIcon />
                    </span></label>
                    <input type="checkbox" id="touch2" />
                    <li class="slide">
                        <li>
                            <FormGroup>
                                <FormControlLabel
                                    control={
                                        <Checkbox {...label} />
                                    }
                                    label="All Users"
                                />
                                <FormControlLabel
                                    control={
                                        <Checkbox {...label} />
                                    }
                                    label="Your Reactions"
                                />
                                <FormControlLabel
                                    control={
                                        <Checkbox {...label} />
                                    }
                                    label="Natile Rothman"
                                />
                                <FormControlLabel
                                    control={
                                        <Checkbox {...label} />
                                    }
                                    label="Robert Hoffman"
                                />
                                <FormControlLabel
                                    control={
                                        <Checkbox {...label} />
                                    }
                                    label="John Smith"
                                />
                                <FormControlLabel
                                    control={
                                        <Checkbox {...label} />
                                    }
                                    label="Tyler Stroll"
                                />
                            </FormGroup>
                        </li>
                    </li>
                </nav>
                <nav>
                    <label for="touch3"><span className="tag-style"><EmojiEmotionsIcon />Emoji
                    <ExpandMoreIcon />
                    </span></label>
                    <input type="checkbox" id="touch3" />
                    <li class="slide">
                        <li>
                            <FormGroup>
                                <FormControlLabel
                                    control={
                                        <Checkbox {...label} />
                                    }
                                    label="⭐"
                                />
                                <FormControlLabel
                                    control={
                                        <Checkbox {...label} />
                                    }
                                    label="🧠"
                                />
                                <FormControlLabel
                                    control={
                                        <Checkbox {...label} />
                                    }
                                    label="😂"
                                />
                                <FormControlLabel
                                    control={
                                        <Checkbox {...label} />
                                    }
                                    label="❓"
                                />
                            </FormGroup>
                        </li>
                    </li>
                </nav>
                <nav>
                    <label for="touch4"><span className="tag-style"><EmojiEmotionsIcon />Sort By
                        <ExpandMoreIcon />
                    </span></label>
                    <input type="checkbox" id="touch4" />
                    <li class="slide">
                        <FormControl>
                              <RadioGroup
                                aria-labelledby="demo-radio-buttons-group-label"
                                defaultValue="Newest to Oldest"
                                name="radio-buttons-group"
                              >
                                <FormControlLabel value="Newest to Oldest" control={<Radio />} label="Newest to Oldest" />
                                <FormControlLabel value="Oldest to Newest" control={<Radio />} label="Oldest to Newest" />
                                <FormControlLabel value="Most reacted" control={<Radio />} label="Most reacted" />
                              </RadioGroup>
                            </FormControl>
                    </li>
                </nav>
            </div>
        </div>

        <div className="structure">
        <Container maxWidth="lg">
            <Box sx={{ bgcolor: '#36393e', height: '100vh' }}>
                <Box
                  sx={{
                    width: "1152px",
                    height: "150px",
                    backgroundColor: '#36393e',
                  }}
                />
                <div className="search-text">
                    <Paper
                        component="form"
                        sx={{ p: '2px 4px', display: 'flex', alignItems: 'right', width: 400 }}>
                        <InputBase
                            sx={{ ml: 1, flex: 1 }}
                            placeholder="Type Here..."
                            inputProps={{ 'aria-label': 'type here' }}
                        />
                    </Paper>
                </div>
                <div className="search-button">
                    <Button sx={{ backgroundColor: '#7289d9' }} variant="contained">Search</Button>
                </div>
            </Box>
        </Container>
        <div className='side-bar-right'>
        </div>
        </div>
      </div>
    );
  }
export default App;
