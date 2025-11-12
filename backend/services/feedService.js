/**
 * Serviço de Feed - FEED-001
 */

const db = require('../database');
const { generateUUID, getCurrentTimestamp } = require('../utils');

/**
 * Cria um novo post no grupo
 * @param {string} userId - ID do usuário
 * @param {string} groupId - ID do grupo
 * @param {string} conteudo - Conteúdo do post
 */
async function createPost(userId, groupId, conteudo) {
  const startTime = Date.now();

  // Validar conteúdo
  if (!conteudo || conteudo.trim().length === 0) {
    throw new Error('Conteúdo é obrigatório');
  }

  if (conteudo.length > 1000) {
    throw new Error('Conteúdo deve ter no máximo 1000 caracteres');
  }

  // Verificar se grupo existe
  const group = await db.get('SELECT id FROM groups WHERE id = ?', [groupId]);
  if (!group) {
    throw new Error('Grupo não encontrado');
  }

  // Verificar se usuário é membro do grupo
  const membership = await db.get(
    'SELECT * FROM group_members WHERE group_id = ? AND user_id = ?',
    [groupId, userId]
  );

  if (!membership) {
    throw new Error('Você precisa ser membro do grupo para postar');
  }

  // Gerar UUID para o post
  const postId = generateUUID();
  const timestamp = getCurrentTimestamp();

  // Inserir post no banco
  await db.run(
    `INSERT INTO posts (id, group_id, author_id, conteudo, created_at)
     VALUES (?, ?, ?, ?, ?)`,
    [postId, groupId, userId, conteudo.trim(), timestamp]
  );

  // Buscar post criado com dados do autor
  const post = await db.get(
    `SELECT 
      p.id,
      p.group_id,
      p.author_id,
      p.conteudo,
      p.created_at,
      u.username as author_username,
      u.email as author_email
     FROM posts p
     JOIN users u ON p.author_id = u.id
     WHERE p.id = ?`,
    [postId]
  );

  const duration = Date.now() - startTime;
  console.log(`✅ Post criado em ${duration}ms`);

  return post;
}

/**
 * Lista posts do grupo (paginado)
 * @param {string} groupId - ID do grupo
 * @param {number} page - Número da página (padrão: 1)
 * @param {number} limit - Posts por página (padrão: 20, máx: 100)
 */
async function getGroupPosts(groupId, page = 1, limit = 20) {
  const startTime = Date.now();

  // Validar parâmetros
  if (page < 1) {
    throw new Error('Página deve ser maior ou igual a 1');
  }

  if (limit < 1 || limit > 100) {
    throw new Error('Limite deve estar entre 1 e 100');
  }

  // Verificar se grupo existe
  const group = await db.get('SELECT id FROM groups WHERE id = ?', [groupId]);
  if (!group) {
    throw new Error('Grupo não encontrado');
  }

  // Calcular offset
  const offset = (page - 1) * limit;

  // Buscar posts paginados (ordem DESC por created_at)
  const posts = await db.all(
    `SELECT 
      p.id,
      p.group_id,
      p.author_id,
      p.conteudo,
      p.created_at,
      u.username as author_username
     FROM posts p
     JOIN users u ON p.author_id = u.id
     WHERE p.group_id = ?
     ORDER BY p.created_at DESC
     LIMIT ? OFFSET ?`,
    [groupId, limit, offset]
  );

  // Contar total de posts
  const totalResult = await db.get(
    'SELECT COUNT(*) as total FROM posts WHERE group_id = ?',
    [groupId]
  );
  const total = totalResult.total;

  // Calcular informações de paginação
  const totalPages = Math.ceil(total / limit);
  const hasNextPage = page < totalPages;
  const hasPrevPage = page > 1;

  const duration = Date.now() - startTime;
  console.log(`✅ ${posts.length} posts recuperados em ${duration}ms`);

  return {
    posts,
    pagination: {
      page,
      limit,
      total,
      totalPages,
      hasNextPage,
      hasPrevPage
    }
  };
}

/**
 * Busca post por ID
 * @param {string} postId - ID do post
 */
async function getPostById(postId) {
  const post = await db.get(
    `SELECT 
      p.id,
      p.group_id,
      p.author_id,
      p.conteudo,
      p.created_at,
      u.username as author_username,
      u.email as author_email,
      g.nome as group_name
     FROM posts p
     JOIN users u ON p.author_id = u.id
     JOIN groups g ON p.group_id = g.id
     WHERE p.id = ?`,
    [postId]
  );

  if (!post) {
    throw new Error('Post não encontrado');
  }

  return post;
}

/**
 * Deleta post (somente o autor)
 * @param {string} postId - ID do post
 * @param {string} userId - ID do usuário
 */
async function deletePost(postId, userId) {
  const startTime = Date.now();

  // Verificar se post existe e se usuário é o autor
  const post = await db.get(
    'SELECT * FROM posts WHERE id = ? AND author_id = ?',
    [postId, userId]
  );

  if (!post) {
    throw new Error('Post não encontrado ou você não tem permissão para deletá-lo');
  }

  // Deletar post
  await db.run('DELETE FROM posts WHERE id = ?', [postId]);

  const duration = Date.now() - startTime;
  console.log(`✅ Post deletado em ${duration}ms`);

  return true;
}

/**
 * Atualiza post (somente o autor)
 * @param {string} postId - ID do post
 * @param {string} userId - ID do usuário
 * @param {string} conteudo - Novo conteúdo
 */
async function updatePost(postId, userId, conteudo) {
  // Verificar se post existe e se usuário é o autor
  const post = await db.get(
    'SELECT * FROM posts WHERE id = ? AND author_id = ?',
    [postId, userId]
  );

  if (!post) {
    throw new Error('Post não encontrado ou você não tem permissão para editá-lo');
  }

  // Validar conteúdo
  if (!conteudo || conteudo.trim().length === 0) {
    throw new Error('Conteúdo é obrigatório');
  }

  if (conteudo.length > 1000) {
    throw new Error('Conteúdo deve ter no máximo 1000 caracteres');
  }

  // Atualizar post
  await db.run(
    'UPDATE posts SET conteudo = ? WHERE id = ?',
    [conteudo.trim(), postId]
  );

  return await getPostById(postId);
}

/**
 * Lista posts do usuário
 * @param {string} userId - ID do usuário
 */
async function getUserPosts(userId) {
  const posts = await db.all(
    `SELECT 
      p.id,
      p.group_id,
      p.conteudo,
      p.created_at,
      g.nome as group_name
     FROM posts p
     JOIN groups g ON p.group_id = g.id
     WHERE p.author_id = ?
     ORDER BY p.created_at DESC`,
    [userId]
  );

  return posts;
}

/**
 * Busca feed geral (últimos posts de todos os grupos que o usuário é membro)
 * @param {string} userId - ID do usuário
 * @param {number} page - Número da página
 * @param {number} limit - Posts por página
 */
async function getUserFeed(userId, page = 1, limit = 20) {
  const startTime = Date.now();

  // Validar parâmetros
  if (page < 1) {
    throw new Error('Página deve ser maior ou igual a 1');
  }

  if (limit < 1 || limit > 100) {
    throw new Error('Limite deve estar entre 1 e 100');
  }

  // Calcular offset
  const offset = (page - 1) * limit;

  // Buscar posts dos grupos que o usuário é membro
  const posts = await db.all(
    `SELECT 
      p.id,
      p.group_id,
      p.author_id,
      p.conteudo,
      p.created_at,
      u.username as author_username,
      g.nome as group_name
     FROM posts p
     JOIN users u ON p.author_id = u.id
     JOIN groups g ON p.group_id = g.id
     WHERE p.group_id IN (
       SELECT group_id FROM group_members WHERE user_id = ?
     )
     ORDER BY p.created_at DESC
     LIMIT ? OFFSET ?`,
    [userId, limit, offset]
  );

  // Contar total de posts
  const totalResult = await db.get(
    `SELECT COUNT(*) as total FROM posts
     WHERE group_id IN (
       SELECT group_id FROM group_members WHERE user_id = ?
     )`,
    [userId]
  );
  const total = totalResult.total;

  // Calcular informações de paginação
  const totalPages = Math.ceil(total / limit);
  const hasNextPage = page < totalPages;
  const hasPrevPage = page > 1;

  const duration = Date.now() - startTime;
  console.log(`✅ Feed de ${posts.length} posts recuperado em ${duration}ms`);

  return {
    posts,
    pagination: {
      page,
      limit,
      total,
      totalPages,
      hasNextPage,
      hasPrevPage
    }
  };
}

module.exports = {
  createPost,
  getGroupPosts,
  getPostById,
  deletePost,
  updatePost,
  getUserPosts,
  getUserFeed
};

