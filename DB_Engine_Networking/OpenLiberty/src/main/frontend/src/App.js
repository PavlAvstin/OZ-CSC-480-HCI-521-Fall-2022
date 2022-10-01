import logo from './logo.svg';
import './App.css';

function httpGetSync(url) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", url, false ); // false for synchronous request
    xmlHttp.send( null );
    return xmlHttp.responseText;
}


//"https://cdn.discordapp.com/avatars/" + localStorage.claims.id + "/" + localStorage.claims.avatar + ".webp"

function App() {
    localStorage.setItem("claims", httpGetSync("http://localhost:9080/api/@me/claims"))
    let jsonClaims = JSON.parse(localStorage.claims);
    localStorage.setItem("usernameDisc", jsonClaims.username + "#" + jsonClaims.discriminator)
    localStorage.setItem("userAvatarUrl", "https://cdn.discordapp.com/avatars/" + jsonClaims.id + "/" + jsonClaims.avatar + ".webp")
  return (
    <div className="App">
      <header className="App-header">
        <img src={localStorage.userAvatarUrl} className="App-logo" alt="logo" />
        <p>
            {"Welcome " + localStorage.usernameDisc}
        </p>
      </header>
    </div>
  );
}

export default App;
