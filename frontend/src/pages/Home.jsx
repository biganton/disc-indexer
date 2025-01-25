import React from "react";
import { Link } from "react-router-dom";
import { Button, Container, Typography } from "@mui/material";

const Home = () => {
  return (
    <Container>
      <Typography variant="h4" gutterBottom>Welcome to the File Manager</Typography>
      <Button component={Link} to="/scan" variant="contained" color="primary">
        Scan Directory
      </Button>
      <Button component={Link} to="/files" variant="contained" color="secondary" style={{ marginLeft: "10px" }}>
        View Files
      </Button>
      <Button component={Link} to="/duplicates" variant="contained" color="warning" style={{ marginLeft: "10px" }}>
        Find Duplicates
      </Button>
      <Button component={Link} to="/versions" variant="contained" color="info" style={{ marginLeft: "10px" }}>
        Find Versions
      </Button>
      <Button component={Link} to="/largest" variant="contained" color="success" style={{ marginLeft: "10px" }}>
            Find Largest Files
      </Button>
      <Button component={Link} to="/logs" variant="contained" color="error" style={{ marginLeft: "10px" }}>
        View Logs
      </Button>
    </Container>
  );
};

export default Home;
