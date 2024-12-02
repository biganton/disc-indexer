import React, { useState } from "react";
import { scanFolder } from "../api/api";
import { Button, TextField, Typography, Container } from "@mui/material";

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
    <Container>
      <Typography variant="h4">Scan Folder</Typography>
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
  );
};

export default ScanFolder;
