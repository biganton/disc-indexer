import React, { useState } from "react";
import { scanFolder } from "../api/api";
import { Button, TextField, Typography, Container } from "@mui/material";
import {Link} from "react-router-dom";
import NavHeader from "../components/NavHeader.jsx";

const ScanFolder = () => {
  const [folderPath, setFolderPath] = useState("");
  const [message, setMessage] = useState("");

  const handleScan = async () => {
    try {
      const response = await scanFolder(folderPath);
      setMessage(response);
    } catch (error) {
      setMessage(error.message);
    }
  };

  return (
    <div>
      <NavHeader pageName="Scan Folder"/>
      <Container style={{ marginLeft: '0', marginRight: 'auto', marginTop: '40px' }}>
      <TextField
        label="Folder Path"
        fullWidth
        value={folderPath}
        onChange={(e) => setFolderPath(e.target.value)}
        style={{ marginBottom: "10px" }}
      />
      <Button variant="contained" onClick={handleScan}>Scan</Button>
      {message && <Typography variant="body1">{message}</Typography>}
      </Container>
    </div>
    )
    ;
};

export default ScanFolder;
