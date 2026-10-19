import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import App from "../App.jsx";
import Style from "../index.css"

createRoot(document.getElementById("root")).render(
    <StrictMode>
        <App />
    </StrictMode>
);