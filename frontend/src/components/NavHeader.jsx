import React from 'react';
import { Button, Typography } from "@mui/material";
import { Link } from "react-router-dom";

const NavHeader = ({ pageName }) => {
  return (
    <div>
      <nav>
        <Button component={Link} to="/" variant="contained" color="primary">
          Home
        </Button>
        <Button component={Link} to="/scan" variant="contained" color="primary" style={{ marginLeft: "10px" }}>
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

      </nav>
      <Typography variant="h4" gutterBottom style={{ marginTop: '20px' }}>
        {pageName}
      </Typography>
    </div>
  );
};

export default NavHeader;
