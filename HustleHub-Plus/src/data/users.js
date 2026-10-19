//https://dev.to/aneeqakhan/create-user-model-3cfb
const { randomUUID } = require("crypto");
const VALID_ROLES = ["freelancer", "client"];
const users = [];

function createUserRecord({ username, email, hashedPassword, role }) {
  return {
    id: randomUUID(),
    username,
    email,
    passwordHash: hashedPassword,
    role,
    createdAt: new Date().toISOString(),
  };
}

function toSafeUser(user) {
  const { passwordHash, ...safeUser } = user;
  return safeUser;
}

function findById(id) {
  return users.find((u) => u.id === id);
}

function findByEmail(email) {
  return users.find((u) => u.email.toLowerCase() === email.toLowerCase());
}

function findByUsername(username) {
  return users.find(
    (u) => u.username.toLowerCase() === username.toLowerCase()
  );
}

function createUser(user) {
  users.push(user);
  return user;
}

function getAllUsers() {
  return users.map(toSafeUser);
}

module.exports = {
  VALID_ROLES,
  createUserRecord,
  toSafeUser,
  findById,
  findByEmail,
  findByUsername,
  createUser,
  getAllUsers,
};