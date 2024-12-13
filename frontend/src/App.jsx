import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import Home from "./pages/Home";
import FileList from "./components/FileList";
import ScanFolder from "./pages/ScanFolder";
import Duplicates from "./pages/Duplicates";
import Versions from "./pages/Versions";
import Largest from "./pages/Largest";

const App = () => {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/files" element={<FileList />} />
        <Route path="/scan" element={<ScanFolder />} />
        <Route path="/duplicates" element={<Duplicates />} />
        <Route path="/versions" element={<Versions />} />
          <Route path="/largest" element={<Largest />} />
      </Routes>
    </Router>
  );
};

export default App;
