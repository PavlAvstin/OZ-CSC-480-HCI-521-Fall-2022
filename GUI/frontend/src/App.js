import logo from './logo.svg';
import './App.css';

function App() {
  return (
    <div className="App">
      <header className='app-header'>
        <button className="home-button">Placeholder Button</button>
        <button className="login-button">Log In</button>
      </header>

      <div className="body-content">
        <div className="logo"></div>
        <div className="description">Save and view important messages on Discord</div>
        <div className="login-text">Log in with Discord</div>
        <button id="login">Log In</button>
      </div>
      {/*<div className='side-bar'></div>*/}
    </div>
  );
}

export default App;
