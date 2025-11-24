/**
 * Serviço de Grupos - GROUP-001
 */

const db = require('../database');
const { generateUUID, getCurrentTimestamp } = require('../utils');

/**
 * Cria um novo grupo e adiciona criador automaticamente
 * @param {string} userId - ID do usuário criador
 * @param {string} nome - Nome do grupo
 * @param {string} descricao - Descrição do grupo
 * @param {string} imagem - Caminho da imagem de capa
 */
async function createGroup(userId, nome, descricao, imagem) {
  const startTime = Date.now();

  // Validar nome
  if (!nome || nome.trim().length === 0) {
    throw new Error('Nome do grupo é obrigatório');
  }

  if (nome.length > 100) {
    throw new Error('Nome do grupo deve ter no máximo 100 caracteres');
  }

  // Verificar se nome é único
  const existingGroup = await db.get(
    'SELECT id FROM groups WHERE LOWER(nome) = LOWER(?)',
    [nome.trim()]
  );

  if (existingGroup) {
    throw new Error('Já existe um grupo com este nome');
  }

  // Gerar UUID para o grupo
  const groupId = generateUUID();
  const timestamp = getCurrentTimestamp();

  // Inserir grupo no banco
  await db.run(
    `INSERT INTO groups (id, nome, descricao, criador, created_at, imagem)
     VALUES (?, ?, ?, ?, ?, ?)`,
    [groupId, nome.trim(), descricao ? descricao.trim() : null, userId, timestamp, imagem || null]
  );

  // Adicionar criador como membro automaticamente
  await db.run(
    `INSERT INTO group_members (group_id, user_id, joined_at)
     VALUES (?, ?, ?)`,
    [groupId, userId, timestamp]
  );

  // Buscar grupo criado com member_count
  const group = await getGroupById(groupId, userId);

  const duration = Date.now() - startTime;
  console.log(`✅ Grupo criado em ${duration}ms`);

  return group;
}

/**
 * Busca grupos por nome (com search opcional)
 * @param {string} search - Termo de busca (opcional)
 */
async function searchGroups(search = null) {
  const startTime = Date.now();

  let query = `
    SELECT 
      g.id,
      g.nome,
      g.descricao,
      g.imagem,
      g.created_at,
      u.username as criador_username,
      COUNT(gm.user_id) as member_count
    FROM groups g
    LEFT JOIN users u ON g.criador = u.id
    LEFT JOIN group_members gm ON g.id = gm.group_id
  `;

  const params = [];

  if (search && search.trim().length > 0) {
    query += ' WHERE LOWER(g.nome) LIKE LOWER(?) OR LOWER(g.descricao) LIKE LOWER(?)';
    const searchTerm = `%${search.trim()}%`;
    params.push(searchTerm, searchTerm);
  }

  query += ' GROUP BY g.id ORDER BY g.created_at DESC';

  const groups = await db.all(query, params);

  const duration = Date.now() - startTime;
  console.log(`✅ ${groups.length} grupos encontrados em ${duration}ms`);

  return groups;
}

/**
 * Busca grupo por ID
 * @param {string} groupId - ID do grupo
 * @param {string} requestingUserId - ID do usuário fazendo a requisição (opcional)
 */
async function getGroupById(groupId, requestingUserId = null) {
  const group = await db.get(
    `SELECT 
      g.id,
      g.nome,
      g.descricao,
      g.imagem,
      g.criador,
      g.created_at,
      u.username as criador_username,
      COUNT(gm.user_id) as member_count
     FROM groups g
     LEFT JOIN users u ON g.criador = u.id
     LEFT JOIN group_members gm ON g.id = gm.group_id
     WHERE g.id = ?
     GROUP BY g.id`,
    [groupId]
  );

  if (!group) {
    throw new Error('Grupo não encontrado');
  }

  // Check membership if requestingUserId is provided
  if (requestingUserId) {
      const isMember = await db.get(
          'SELECT 1 FROM group_members WHERE group_id = ? AND user_id = ?',
          [groupId, requestingUserId]
      );
      group.is_member = !!isMember;
  } else {
      group.is_member = false;
  }

  return group;
}

/**
 * Adiciona usuário ao grupo
 * @param {string} groupId - ID do grupo
 * @param {string} userId - ID do usuário
 */
async function joinGroup(groupId, userId) {
  const startTime = Date.now();

  // Verificar se grupo existe
  const group = await db.get('SELECT id FROM groups WHERE id = ?', [groupId]);
  if (!group) {
    throw new Error('Grupo não encontrado');
  }

  // Verificar se usuário já é membro
  const membership = await db.get(
    'SELECT * FROM group_members WHERE group_id = ? AND user_id = ?',
    [groupId, userId]
  );

  if (membership) {
    throw new Error('Você já é membro deste grupo');
  }

  // Adicionar usuário ao grupo
  const timestamp = getCurrentTimestamp();
  await db.run(
    `INSERT INTO group_members (group_id, user_id, joined_at)
     VALUES (?, ?, ?)`,
    [groupId, userId, timestamp]
  );

  // Buscar grupo atualizado
  const updatedGroup = await getGroupById(groupId, userId);

  const duration = Date.now() - startTime;
  console.log(`✅ Usuário entrou no grupo em ${duration}ms`);

  return updatedGroup;
}

/**
 * Remove usuário do grupo
 * @param {string} groupId - ID do grupo
 * @param {string} userId - ID do usuário
 */
async function leaveGroup(groupId, userId) {
  const startTime = Date.now();

  // Verificar se grupo existe
  const group = await db.get('SELECT * FROM groups WHERE id = ?', [groupId]);
  if (!group) {
    throw new Error('Grupo não encontrado');
  }

  // Verificar se é o criador
  if (group.criador === userId) {
    throw new Error('O criador não pode sair do grupo. Você deve deletar o grupo.');
  }

  // Verificar se usuário é membro
  const membership = await db.get(
    'SELECT * FROM group_members WHERE group_id = ? AND user_id = ?',
    [groupId, userId]
  );

  if (!membership) {
    throw new Error('Você não é membro deste grupo');
  }

  // Remover usuário do grupo
  await db.run(
    'DELETE FROM group_members WHERE group_id = ? AND user_id = ?',
    [groupId, userId]
  );

  // Buscar grupo atualizado
  const updatedGroup = await getGroupById(groupId, userId);

  const duration = Date.now() - startTime;
  console.log(`✅ Usuário saiu do grupo em ${duration}ms`);

  return updatedGroup;
}

/**
 * Lista membros de um grupo
 * @param {string} groupId - ID do grupo
 */
async function getGroupMembers(groupId) {
  // Verificar se grupo existe
  const group = await db.get('SELECT id FROM groups WHERE id = ?', [groupId]);
  if (!group) {
    throw new Error('Grupo não encontrado');
  }

  const members = await db.all(
    `SELECT 
      u.id,
      u.username,
      u.email,
      gm.joined_at,
      CASE WHEN g.criador = u.id THEN 1 ELSE 0 END as is_creator
     FROM group_members gm
     JOIN users u ON gm.user_id = u.id
     JOIN groups g ON gm.group_id = g.id
     WHERE gm.group_id = ?
     ORDER BY gm.joined_at ASC`,
    [groupId]
  );

  return members;
}

/**
 * Verifica se usuário é membro do grupo
 * @param {string} groupId - ID do grupo
 * @param {string} userId - ID do usuário
 */
async function isMember(groupId, userId) {
  const membership = await db.get(
    'SELECT * FROM group_members WHERE group_id = ? AND user_id = ?',
    [groupId, userId]
  );

  return !!membership;
}

/**
 * Deleta grupo (somente o criador)
 * @param {string} groupId - ID do grupo
 * @param {string} userId - ID do usuário
 */
async function deleteGroup(groupId, userId) {
  // Verificar se grupo existe
  const group = await db.get('SELECT * FROM groups WHERE id = ?', [groupId]);
  if (!group) {
    throw new Error('Grupo não encontrado');
  }

  // Verificar se é o criador
  if (group.criador !== userId) {
    throw new Error('Apenas o criador pode deletar o grupo');
  }

  // Deletar grupo (cascade deleta membros e posts automaticamente)
  await db.run('DELETE FROM groups WHERE id = ?', [groupId]);

  console.log(`✅ Grupo ${groupId} deletado`);
  return true;
}

/**
 * Atualiza grupo (somente o criador)
 * @param {string} groupId - ID do grupo
 * @param {string} userId - ID do usuário
 * @param {Object} updates - Campos a atualizar
 */
async function updateGroup(groupId, userId, updates) {
  // Verificar se grupo existe e se usuário é o criador
  const group = await db.get(
    'SELECT * FROM groups WHERE id = ? AND criador = ?',
    [groupId, userId]
  );

  if (!group) {
    throw new Error('Grupo não encontrado ou você não tem permissão para editá-lo');
  }

  const { nome, descricao, imagem } = updates;
  const fieldsToUpdate = [];
  const values = [];

  if (nome) {
    if (nome.length > 100) {
      throw new Error('Nome do grupo deve ter no máximo 100 caracteres');
    }

    // Verificar se novo nome já existe (exceto o atual)
    const existingGroup = await db.get(
      'SELECT id FROM groups WHERE LOWER(nome) = LOWER(?) AND id != ?',
      [nome.trim(), groupId]
    );

    if (existingGroup) {
      throw new Error('Já existe um grupo com este nome');
    }

    fieldsToUpdate.push('nome = ?');
    values.push(nome.trim());
  }

  if (descricao !== undefined) {
    fieldsToUpdate.push('descricao = ?');
    values.push(descricao ? descricao.trim() : null);
  }

  if (imagem !== undefined) {
      fieldsToUpdate.push('imagem = ?');
      values.push(imagem);
  }

  if (fieldsToUpdate.length === 0) {
    throw new Error('Nenhum campo para atualizar');
  }

  values.push(groupId);

  await db.run(
    `UPDATE groups SET ${fieldsToUpdate.join(', ')} WHERE id = ?`,
    values
  );

  return await getGroupById(groupId, userId);
}

module.exports = {
  createGroup,
  searchGroups,
  getGroupById,
  joinGroup,
  leaveGroup,
  getGroupMembers,
  isMember,
  deleteGroup,
  updateGroup
};
