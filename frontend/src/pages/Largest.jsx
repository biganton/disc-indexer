import React from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Button,
} from '@mui/material';
import { openFile, handleDelete } from '../actions/fileActions.js';
import NavHeader from '../components/NavHeader.jsx';

const fetchLargest = async () => {
  const res = await fetch('http://localhost:8080/files/largest');
  if (!res.ok) {
    throw new Error('Failed to fetch largest files');
  }
  return res.json();
};

const Largest = () => {
  const { data, error, isLoading, refetch} = useQuery({
    queryKey: ['largest'],
    queryFn: fetchLargest,
  });

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error.message}</div>;
  }

  return (
      <div>
        <NavHeader pageName="Largest Files"/>
        <TableContainer component={Paper} style={{marginBottom: '20px'}}>
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
                        style={{ margin: '10px' }}
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
      </div>
  );
};

export default Largest;
