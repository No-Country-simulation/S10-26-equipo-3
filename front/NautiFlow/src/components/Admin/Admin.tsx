import Login from "../Login/Login";
import Splash from "../Splash/Splah";
import { useState } from "react";

export default function Admin(){
     const [showSplash, setShowSplash] = useState(true);
    return(
        <>
         <Login />
      {showSplash && <Splash onFinish={() => setShowSplash(false)} />}
        </>
    )

}