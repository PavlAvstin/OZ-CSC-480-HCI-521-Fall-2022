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

function SideBar({guilds, channels, users, reactions, selectGuild, changeSelectedChannels, changeSelectedReactions}) {

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
  return (
    <div className="side-bar"> 
      {guildsDisplay()}
      {channelsDisplay()}
      {reactionsDisplay()}
    </div>
  )
}
export default SideBar;