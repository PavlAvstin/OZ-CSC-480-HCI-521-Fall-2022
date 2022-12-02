import { React } from 'react';
import Avatar from '@mui/material/Avatar';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import ListItemText from '@mui/material/ListItemText';
import DialogTitle from '@mui/material/DialogTitle';
import Dialog from '@mui/material/Dialog';

function ShareDialog(props) {
  const { onClose, open, users } = props;

  const handleClose = () => {
    onClose(null);
  };

  const handleListItemClick = (user_id) => {
    onClose(user_id);
  };

  function userList() {
    if(users == null) return;
    const list = users.map((user) => (
      <ListItem button onClick={() => handleListItemClick(user.discord_id)} key={user.discord_id}>
        <ListItemAvatar>
          <Avatar src={"https://cdn.discordapp.com/avatars/" + user.discord_id + "/" + user.avatar_hash + ".png"}>
          </Avatar>
        </ListItemAvatar>
        <ListItemText primary={user.author_nickname} />
      </ListItem>
    )); 
    return list;
  }

  return (
    <Dialog onClose={handleClose} open={open} scroll="paper">
      <DialogTitle>Select user to send message to.</DialogTitle>
      <List sx={{ pt: 0 }}>
        {userList()}
      </List>
    </Dialog>
  );
}
export default ShareDialog;