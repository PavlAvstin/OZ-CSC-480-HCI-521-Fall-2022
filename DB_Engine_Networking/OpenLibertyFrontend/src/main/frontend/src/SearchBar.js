import { React } from 'react';
import {useState} from 'react';
import Paper from '@mui/material/Paper';
import InputBase from '@mui/material/InputBase';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';
import SearchIcon from '@mui/icons-material/Search';
import CloseIcon from '@mui/icons-material/Close';
function SearchBar({handleSearch}) {
  const [value, setValue] = useState("");
  function clear() {
    setValue("");
    handleSearch("");
  }
  function handleSubmit(e) {
    e.preventDefault();
    search();
  }
  function changeVal(e) {
    setValue(e.target.value);
  }
  function search() {
    handleSearch(value);
  }
  return (
    <Paper
      component="form"
      justifyContent="center"
      sx={{display: 'flex', alignItems: 'center', width: '50%', margin: 'auto', backgroundColor: "rgb(250, 250, 250)" }}
      onSubmit={handleSubmit}
    >
      <InputBase
        sx={{ ml: 1, flex: 1 }}
        placeholder="Search Messages"
        value={value}
        inputProps={{ 'aria-label': 'Search Messages' }}
        onChange={changeVal}
      />
      <IconButton type="button" sx={{ p: '10px' }} aria-label="search" onClick={() => search()}>
        <SearchIcon />
      </IconButton>
      <Divider sx={{ height: 28, m: 0.5 }} orientation="vertical" />
      <IconButton color="primary" sx={{ p: '10px' }} aria-label="directions" onClick={() => clear()}>
        <CloseIcon />
      </IconButton>
    </Paper>
  );
}
export default SearchBar;