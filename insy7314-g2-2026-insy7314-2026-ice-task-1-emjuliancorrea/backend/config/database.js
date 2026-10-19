const mongoose = require('mongoose');

async function connectDB() {
  const uri = process.env.MONGODB_URI;

  if (!uri) {
    throw new Error(
      'MONGODB_URI not defined. Check your .env file'
    );
  }

  try {
    const conn = await mongoose.connect(uri);
    console.log(
      `MongoDB connected: host=${conn.connection.host}, db=${conn.connection.name}`
    );

    return conn;
  } catch (err) {
    throw new Error(`MongoDB connection failed: ${err.message}`);
  }
}

module.exports = connectDB;