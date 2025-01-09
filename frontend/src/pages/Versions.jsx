import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  Button,
  Checkbox,
} from '@mui/material';
import { openFile, handleDelete } from '../actions/fileActions.js';
import {Link} from "react-router-dom";
import NavHeader from "../components/NavHeader.jsx";

const fetchVersions = async () => {
  const res = await fetch('http://localhost:8080/files/versions?threshold=3');
  if (!res.ok) {
    throw new Error('Failed to fetch versions');
  }
  return res.json();
};

const Versions = () => {
  const { data, error, isLoading, refetch } = useQuery({
    queryKey: ['versions'],
    queryFn: fetchVersions,
  });

  const [selectedFiles, setSelectedFiles] = useState([]);
  const [archive, setArchive] = useState(false);
  const [targetDirectoryPath, setTargetDirectoryPath] = useState('');

  const toggleFileSelection = (fileId) => {
    setSelectedFiles((prevSelected) =>
      prevSelected.includes(fileId)
        ? prevSelected.filter((id) => id !== fileId)
        : [...prevSelected, fileId]
    );
  };

  const handleMoveFiles = async (targetDirectoryPath, archive) => {
    try {
      const response = await fetch(
        `http://localhost:8080/files/move-to-directory`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ fileIds: selectedFiles, targetDirectoryPath, archive }),
        }
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      alert(archive ? 'Files moved and directory archived successfully!' : 'Files moved successfully!');
      refetch();
    } catch (err) {
      alert(`Error: ${err.message}`);
    }
  };

  const handleMoveToGroupedDirectories = async (targetDirectoryPath, threshold = 3, archive) => {
    try {
      const response = await fetch(
          `http://localhost:8080/files/versions/move-to-grouped-directories`,
          {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ targetDirectoryPath, threshold, archive }),
          }
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      alert(archive ? 'Files moved and directory archived successfully!' : 'Files moved successfully!');
      refetch();
    } catch (err) {
      alert(`Error: ${err.message}`);
    }
  };


  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error.message}</div>;
  }

  return (
      <div>
        <NavHeader pageName="File Versions"/>
        {data.map((group, index) => (
              <TableContainer component={Paper} key={index} style={{marginBottom: '20px'}}>
                  <Typography variant="h6" style={{padding: '10px'}}>
                      Group {index + 1}
                  </Typography>
                  <Table>
                      <TableHead>
                          <TableRow>
                              <TableCell>Select</TableCell>
                              <TableCell>ID</TableCell>
                              <TableCell>File Name</TableCell>
                              <TableCell>File Path</TableCell>
                              <TableCell>Size</TableCell>
                              <TableCell>Hash</TableCell>
                              <TableCell>Created At</TableCell>
                              <TableCell>Last Modified</TableCell>
                              <TableCell>Actions</TableCell>
                          </TableRow>
                      </TableHead>
                      <TableBody>
                          {group.map((file) => (
                              <TableRow key={file.id}>
                                  <TableCell>
                                      <Checkbox
                                          checked={selectedFiles.includes(file.id)}
                                          onChange={() => toggleFileSelection(file.id)}
                                      />
                                  </TableCell>
                                  <TableCell>{file.id}</TableCell>
                                  <TableCell>{file.fileName}</TableCell>
                                  <TableCell>{file.filePath}</TableCell>
                                  <TableCell>{file.size}</TableCell>
                                  <TableCell>{file.hash}</TableCell>
                                  <TableCell>{new Date(file.createdAt).toLocaleString()}</TableCell>
                                  <TableCell>{new Date(file.lastModified).toLocaleString()}</TableCell>
                                  <TableCell>
                                      <Button
                                          variant="contained"
                                          color="primary"
                                          style={{margin: '10px'}}
                                          onClick={() => openFile(file.filePath)}
                                      >
                                          Open
                                      </Button>
                                      <Button
                                          variant="contained"
                                          color="primary"
                                          onClick={() => handleDelete(file.id, refetch)}
                                      >
                                          Delete
                                      </Button>
                                  </TableCell>
                              </TableRow>
                          ))}
                      </TableBody>
                  </Table>
              </TableContainer>
          ))}
          <div style={{margin: '20px', display: 'flex', alignItems: 'center'}}>
              <Checkbox
                  checked={archive}
                  onChange={() => setArchive((prev) => !prev)}
              />
              <Typography variant="body1">Archive directory after moving files</Typography>
          </div>
          <Button
              variant="contained"
              color="primary"
              style={{margin: '10px'}}
              onClick={() => {
                  const targetDirectoryPath = prompt('Enter target directory path:');
                  if (targetDirectoryPath) {
                      handleMoveToGroupedDirectories(targetDirectoryPath, 3, archive);
                  } else {
                      alert('Please enter a valid directory path.');
                  }
              }}
          >
              Move Files To Grouped Directories
          </Button>

          <Button
              variant="contained"
              color="primary"
              style={{margin: '10px'}}
              onClick={() => {
                  const targetDirectoryPath = prompt('Enter target directory path:');
                  if (targetDirectoryPath) {
                      handleMoveFiles(targetDirectoryPath, archive);
                  }
              }}
              disabled={selectedFiles.length === 0}
          >
              Move Selected Files To Directory
          </Button>
      </div>
  );
};

export default Versions;
