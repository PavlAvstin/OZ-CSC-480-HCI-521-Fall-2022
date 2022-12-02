import React from "react";
import IosShareIcon from '@mui/icons-material/IosShare';
import Avatar from '@mui/material/Avatar';
import "./Message.css";
function Message({message, filters, dictionary, shareMenu}) {
    const author = message.author;
    const authorName = author.author_nickname;
    const authorAvatar = "https://cdn.discordapp.com/avatars/" + author.discord_id + "/" + author.avatar_hash + ".png";
    const content = message.content;
    const reactions = message.reactions;
    function timeDisplay() {
        const currentTime = Date.now();
        const updatedTime = new Date(message.updated_at);
        var difference = currentTime - updatedTime;
        difference = Math.floor(difference / 1000);
        if(difference < 60) return (
            <div className="time">{difference} seconds ago</div>
        )
        difference = Math.floor(difference / 60);
        if(difference < 60) return (
            <div className="time">{difference} minutes ago</div>
        )
        difference = Math.floor(difference / 60);
        if(difference < 24) return (
            <div className="time">{difference} hours ago</div>
        )
        difference = Math.floor(difference / 24);
        return (
            <div className="time">{difference} days ago</div>
        )
    }
    function hasReaction(emoji) {
        for(const reaction of reactions) {
            if(reaction.dictionary_emoji === emoji) {
                return true;
            }
        }
        return false;
    }
    function reactionDisplay() {
        var text ="";
        if(filters.reactions.length == 0) {
            dictionary.forEach(entry => {
                if(hasReaction(entry.emoji))
                    text = text + entry.emoji + " ";
            })
        }
        else {
            Array.from(filters.reactions).forEach(emoji => {
                if(hasReaction(emoji))
                    text = text + emoji + " ";
            })
        }
        return (
            <div className="reactions">{text}</div>
        )
    }
    return (
        <div className="message">
            <div className="messageHeader">
                <div className="avatar"><Avatar src={authorAvatar} sx={{width: "inherit", height: "inherit"}}></Avatar></div>
                <div className="authorName">{authorName}</div>
                {timeDisplay()}
                <button className="shareButton clickable" onClick={() => shareMenu(message.discord_id)}><IosShareIcon/></button>
            </div>
            <div className="messageContent">
                {content}
                {reactionDisplay()}
            </div>

        </div>
    );
}
export default Message;