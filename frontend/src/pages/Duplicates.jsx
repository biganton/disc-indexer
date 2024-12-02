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
  Typography,
} from '@mui/material';

const fetchDuplicates = async () => {
  const res = await fetch('http://localhost:8080/files/duplicates');
  if (!res.ok) {
    throw new Error('Failed to fetch duplicates');
  }
  return res.json();
};

const Duplicates = () => {
  const { data, error, isLoading } = useQuery({
    queryKey: ['duplicates'],
    queryFn: fetchDuplicates,
  });

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error.message}</div>;
  }

  return (
    <div>
      <Typography variant="h4" gutterBottom>
        Duplicate Files
      </Typography>
      {data.map((group, index) => (
        <TableContainer component={Paper} key={index} style={{ marginBottom: '20px' }}>
          <Typography variant="h6" style={{ padding: '10px' }}>
            Group {index + 1}
          </Typography>
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
              </TableRow>
            </TableHead>
            <TableBody>
              {group.map((file) => (
                <TableRow key={file.id}>
                  <TableCell>{file.id}</TableCell>
                  <TableCell>{file.fileName}</TableCell>
                  <TableCell>{file.filePath}</TableCell>
                  <TableCell>{file.size}</TableCell>
                  <TableCell>{file.hash}</TableCell>
                  <TableCell>{new Date(file.createdAt).toLocaleString()}</TableCell>
                  <TableCell>{new Date(file.lastModified).toLocaleString()}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      ))}
    </div>
  );
};

export default Duplicates;