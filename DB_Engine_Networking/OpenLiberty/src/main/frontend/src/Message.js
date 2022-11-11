import React from "react";
import "./Message.css";
function Message(message) {
    const author = message.message.author;
    const authorName = author.author_nickname;
    const authorAvatar = "https://cdn.discordapp.com/avatars/" + author.discord_id + "/" + author.avatar_hash + ".png";
    const content = message.message.content;
    const reactions = message.message.reactions;
    const reactionAuthor = reactions[0].author.author_nickname;
    const reactionEmoji = reactions[0].dictionary_emoji;
    return (
        <div className="message">
            <div className="messageHeader">
                <img src={authorAvatar}/>
                {authorName}
            </div>
            <div className="messageContent">{content}</div>
            <div classNames="reaction">{reactionAuthor} reacted with {reactionEmoji}</div>
        </div>
    );
}
export default Message;