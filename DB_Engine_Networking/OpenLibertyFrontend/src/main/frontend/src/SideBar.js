import React from "react";
import "./SideBar.css";
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import Checkbox from '@mui/material/Checkbox';
import FormControl from '@mui/material/FormControl';
import FormGroup from '@mui/material/FormGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';

function SideBar({guilds, channels, users, reactions, selectGuild, changeSelectedChannels, changeSelectedReactions, changeSelectedUsers, changeSortOrder}) {

  function guildsDisplay() {
    if(guilds != null) {
      const list = guilds.map((guild) =>
        <ListItem key={guild.name}>
          <ListItemButton  onClick={(e) => {selectGuild(guild.id)}}>
            <ListItemText primary={guild.name}/>
          </ListItemButton>
        </ListItem>
      )
      return (
        <nav>
          <label for="guilds"><span className="tag-style clickable">Guilds</span></label>
          <input type="checkbox" id="guilds" className="dropdown"/>
          <li className="slide">{list}</li>
        </nav>
      )
    }
  }
  
  function channelsDisplay() {
    if(channels != null) {
      const list = channels.filter(channel => channel.type === 0).map((channel) => 
        <FormGroup>
          <FormControlLabel 
            label={channel.name} control={<Checkbox
              onChange={(e) => {changeSelectedChannels(channel.id, e.target.checked)}}/>}
          />
        </FormGroup>
      )
      return (
        <nav>
          <label for="channels"><span className="tag-style clickable">Channels</span></label>
          <input type="checkbox" id="channels" className="dropdown"/>
          <li className="slide">{list}</li>
        </nav>
      )
    }
  } 

  function userDisplay() {
    if(users != null) {
      const list = users.map((user) =>
      <FormGroup>
        <FormControlLabel label={user.author_nickname} control={<Checkbox
          onChange={(e) => {changeSelectedUsers(user.discord_id, e.target.checked)}}/>}  
        />
      </FormGroup>
      )
      return (
        <nav>
          <label for="users"><span className="tag-style clickable">Users</span></label>
          <input type="checkbox" id="users" className="dropdown"/>
          <li className="slide">{list}</li>
        </nav>
      )
    }
  }
  function reactionsDisplay() {
    if(reactions != null) {
      const list = reactions.map((reaction) => 
        <FormGroup>
          <FormControlLabel label={reaction.emoji + " " + reaction.meaning} control={<Checkbox 
            onChange={(e) => {changeSelectedReactions(reaction.emoji, e.target.checked)}}/>} 
          />
        </FormGroup>
      )
      return (
        <nav>
          <label for="reactions"><span className="tag-style clickable">Reactions</span></label>
          <input type="checkbox" id="reactions" className="dropdown"/>
          <li className="slide">{list}</li>
        </nav>
      )
    }
  }

  function sortDisplay() {
    if(channels != null) {
      return (
        <nav>
          <label for="sort"><span className="tag-style clickable">Sort</span></label>
          <input type="checkbox" id="sort" className="dropdown"/>
          <li className="slide">
            <FormControl>
              <RadioGroup
                defaultValue="Newest to Oldest"
                name="radio-button-group"
              >
                <FormControlLabel value="Newest to Oldest" control={<Radio onClick={(e) => changeSortOrder(false)}/>} label="Newest to Oldest" />
                <FormControlLabel value="Oldest to Newest" control={<Radio onClick={(e) => changeSortOrder(true)}/>} label="Oldest to Newest" />

              </RadioGroup>
            </FormControl>
          </li>
        </nav>
      )
    }
  }
  return (
    <div className="side-bar"> 
      {guildsDisplay()}
      {channelsDisplay()}
      {userDisplay()}
      {reactionsDisplay()}
      {sortDisplay()}
    </div>
  )
}
export default SideBar;