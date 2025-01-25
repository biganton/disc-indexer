import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Button, Typography, TextField, Box } from '@mui/material';
import { openFile, handleDelete } from '../actions/fileActions.js';
import NavHeader from './NavHeader.jsx';

const fetchFiles = async (keyword = '') => {
  const url = keyword
      ? `http://localhost:8080/files/search?keyword=${encodeURIComponent(keyword)}`
      : 'http://localhost:8080/files/all';
  const res = await fetch(url);
  if (!res.ok) {
    throw new Error('Failed to fetch files');
  }
  return res.json();
};

const FileList = () => {
  const [searchKeyword, setSearchKeyword] = useState('');
  const [currentKeyword, setCurrentKeyword] = useState('');
  const { data, error, isLoading, refetch } = useQuery({
    queryKey: ['files', currentKeyword],
    queryFn: () => fetchFiles(currentKeyword),
  });

  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentKeyword(searchKeyword);
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error.message}</div>;
  }

  return (
      <div>
        <NavHeader pageName="All Files" />
        <Box component="form" onSubmit={handleSearch} display="flex" alignItems="center" gap={2} mb={2}>
          <TextField
              label="Search keyword in files"
              variant="outlined"
              size="small"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
          />
          <Button type="submit" variant="contained" color="primary">
            Search
          </Button>
        </Box>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
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
              {data.map((file) => (
                  <TableRow key={file.id}>
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
                          style={{ marginRight: '10px' }}
                          onClick={() => openFile(file.filePath)}
                      >
                        Open
                      </Button>
                      <Button
                          variant="contained"
                          color="secondary"
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
      </div>
  );
};

export default FileList;
