/**
 * Testes unitários para feedService.js
 */

const feedService = require('../../../services/feedService');
const db = require('../../../database');

// Mock do banco de dados
jest.mock('../../../database');

describe('FeedService - createPost', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve criar post com sucesso', async () => {
    const mockGroup = { id: 'group-123' };
    const mockMembership = { group_id: 'group-123', user_id: 'user-123' };
    const mockPost = {
      id: 'post-123',
      group_id: 'group-123',
      author_id: 'user-123',
      conteudo: 'Conteúdo do post',
      created_at: '2024-01-15T10:00:00.000Z',
      author_username: 'usuario'
    };

    db.get.mockResolvedValueOnce(mockGroup); // grupo existe
    db.get.mockResolvedValueOnce(mockMembership); // é membro
    db.run.mockResolvedValueOnce({}); // insert post
    db.get.mockResolvedValueOnce(mockPost); // post criado

    const result = await feedService.createPost(
      'user-123',
      'group-123',
      'Conteúdo do post'
    );

    expect(result.conteudo).toBe('Conteúdo do post');
    expect(result.group_id).toBe('group-123');
    expect(db.run).toHaveBeenCalledTimes(1);
  });

  test('deve rejeitar conteúdo vazio', async () => {
    await expect(
      feedService.createPost('user-123', 'group-123', '')
    ).rejects.toThrow('Conteúdo é obrigatório');
  });

  test('deve rejeitar conteúdo muito longo', async () => {
    const longContent = 'a'.repeat(1001);
    await expect(
      feedService.createPost('user-123', 'group-123', longContent)
    ).rejects.toThrow('no máximo 1000 caracteres');
  });

  test('deve rejeitar se grupo não existir', async () => {
    db.get.mockResolvedValueOnce(null); // grupo não existe

    await expect(
      feedService.createPost('user-123', 'group-inexistente', 'Conteúdo')
    ).rejects.toThrow('Grupo não encontrado');
  });

  test('deve rejeitar se usuário não for membro', async () => {
    const mockGroup = { id: 'group-123' };

    db.get.mockResolvedValueOnce(mockGroup); // grupo existe
    db.get.mockResolvedValueOnce(null); // não é membro

    await expect(
      feedService.createPost('user-123', 'group-123', 'Conteúdo')
    ).rejects.toThrow('Você precisa ser membro do grupo para postar');
  });
});

describe('FeedService - getGroupPosts', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve retornar posts do grupo paginados', async () => {
    const mockGroup = { id: 'group-123' };
    const mockPosts = [
      {
        id: 'post-1',
        conteudo: 'Post 1',
        created_at: '2024-01-15T10:00:00.000Z',
        author_username: 'user1'
      },
      {
        id: 'post-2',
        conteudo: 'Post 2',
        created_at: '2024-01-14T10:00:00.000Z',
        author_username: 'user2'
      }
    ];

    db.get.mockResolvedValueOnce(mockGroup); // grupo existe
    db.all.mockResolvedValueOnce(mockPosts); // posts
    db.get.mockResolvedValueOnce({ total: 2 }); // count

    const result = await feedService.getGroupPosts('group-123', 1, 20);

    expect(result.posts.length).toBe(2);
    expect(result.pagination.page).toBe(1);
    expect(result.pagination.total).toBe(2);
    expect(result.pagination.totalPages).toBe(1);
  });

  test('deve rejeitar página inválida', async () => {
    await expect(
      feedService.getGroupPosts('group-123', 0, 20)
    ).rejects.toThrow('Página deve ser maior ou igual a 1');
  });

  test('deve rejeitar limite inválido', async () => {
    await expect(
      feedService.getGroupPosts('group-123', 1, 0)
    ).rejects.toThrow('Limite deve estar entre 1 e 100');

    await expect(
      feedService.getGroupPosts('group-123', 1, 101)
    ).rejects.toThrow('Limite deve estar entre 1 e 100');
  });

  test('deve rejeitar se grupo não existir', async () => {
    db.get.mockResolvedValueOnce(null);

    await expect(
      feedService.getGroupPosts('group-inexistente', 1, 20)
    ).rejects.toThrow('Grupo não encontrado');
  });

  test('deve calcular paginação corretamente', async () => {
    const mockGroup = { id: 'group-123' };
    const mockPosts = [];

    db.get.mockResolvedValueOnce(mockGroup);
    db.all.mockResolvedValueOnce(mockPosts);
    db.get.mockResolvedValueOnce({ total: 50 }); // 50 posts totais

    const result = await feedService.getGroupPosts('group-123', 2, 20);

    expect(result.pagination.totalPages).toBe(3);
    expect(result.pagination.hasNextPage).toBe(true);
    expect(result.pagination.hasPrevPage).toBe(true);
  });
});

describe('FeedService - getPostById', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve retornar post por ID', async () => {
    const mockPost = {
      id: 'post-123',
      conteudo: 'Conteúdo do post',
      author_username: 'usuario',
      group_name: 'Grupo Teste'
    };

    db.get.mockResolvedValueOnce(mockPost);

    const result = await feedService.getPostById('post-123');

    expect(result.id).toBe('post-123');
    expect(result.conteudo).toBe('Conteúdo do post');
  });

  test('deve rejeitar ID não encontrado', async () => {
    db.get.mockResolvedValueOnce(null);

    await expect(
      feedService.getPostById('post-inexistente')
    ).rejects.toThrow('Post não encontrado');
  });
});

describe('FeedService - deletePost', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve deletar post com sucesso', async () => {
    const mockPost = {
      id: 'post-123',
      author_id: 'user-123'
    };

    db.get.mockResolvedValueOnce(mockPost);
    db.run.mockResolvedValueOnce({});

    const result = await feedService.deletePost('post-123', 'user-123');

    expect(result).toBe(true);
    expect(db.run).toHaveBeenCalledWith(
      'DELETE FROM posts WHERE id = ?',
      ['post-123']
    );
  });

  test('deve rejeitar se não for o autor', async () => {
    db.get.mockResolvedValueOnce(null);

    await expect(
      feedService.deletePost('post-123', 'user-outro')
    ).rejects.toThrow('não tem permissão');
  });
});

describe('FeedService - updatePost', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve atualizar post com sucesso', async () => {
    const mockPost = {
      id: 'post-123',
      author_id: 'user-123',
      conteudo: 'Conteúdo antigo'
    };

    const mockUpdatedPost = {
      id: 'post-123',
      conteudo: 'Novo conteúdo',
      author_username: 'usuario'
    };

    db.get.mockResolvedValueOnce(mockPost); // verificar permissão
    db.run.mockResolvedValueOnce({}); // update
    db.get.mockResolvedValueOnce(mockUpdatedPost); // getPostById

    const result = await feedService.updatePost(
      'post-123',
      'user-123',
      'Novo conteúdo'
    );

    expect(result.conteudo).toBe('Novo conteúdo');
  });

  test('deve rejeitar se não for o autor', async () => {
    db.get.mockResolvedValueOnce(null);

    await expect(
      feedService.updatePost('post-123', 'user-outro', 'Novo conteúdo')
    ).rejects.toThrow('não tem permissão');
  });

  test('deve rejeitar conteúdo vazio', async () => {
    const mockPost = {
      id: 'post-123',
      author_id: 'user-123'
    };

    db.get.mockResolvedValueOnce(mockPost);

    await expect(
      feedService.updatePost('post-123', 'user-123', '')
    ).rejects.toThrow('Conteúdo é obrigatório');
  });

  test('deve rejeitar conteúdo muito longo', async () => {
    const mockPost = {
      id: 'post-123',
      author_id: 'user-123'
    };

    db.get.mockResolvedValueOnce(mockPost);

    const longContent = 'a'.repeat(1001);
    await expect(
      feedService.updatePost('post-123', 'user-123', longContent)
    ).rejects.toThrow('no máximo 1000 caracteres');
  });
});

describe('FeedService - getUserPosts', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve retornar posts do usuário', async () => {
    const mockPosts = [
      {
        id: 'post-1',
        conteudo: 'Post 1',
        group_name: 'Grupo A'
      },
      {
        id: 'post-2',
        conteudo: 'Post 2',
        group_name: 'Grupo B'
      }
    ];

    db.all.mockResolvedValueOnce(mockPosts);

    const result = await feedService.getUserPosts('user-123');

    expect(Array.isArray(result)).toBe(true);
    expect(result.length).toBe(2);
  });

  test('deve retornar array vazio se usuário não tem posts', async () => {
    db.all.mockResolvedValueOnce([]);

    const result = await feedService.getUserPosts('user-sem-posts');

    expect(Array.isArray(result)).toBe(true);
    expect(result.length).toBe(0);
  });
});

describe('FeedService - getUserFeed', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve retornar feed do usuário', async () => {
    const mockPosts = [
      {
        id: 'post-1',
        conteudo: 'Post 1',
        author_username: 'user1',
        group_name: 'Grupo A'
      },
      {
        id: 'post-2',
        conteudo: 'Post 2',
        author_username: 'user2',
        group_name: 'Grupo B'
      }
    ];

    db.all.mockResolvedValueOnce(mockPosts); // posts
    db.get.mockResolvedValueOnce({ total: 2 }); // count

    const result = await feedService.getUserFeed('user-123', 1, 20);

    expect(result.posts.length).toBe(2);
    expect(result.pagination.total).toBe(2);
  });

  test('deve rejeitar página inválida', async () => {
    await expect(
      feedService.getUserFeed('user-123', 0, 20)
    ).rejects.toThrow('Página deve ser maior ou igual a 1');
  });

  test('deve rejeitar limite inválido', async () => {
    await expect(
      feedService.getUserFeed('user-123', 1, 0)
    ).rejects.toThrow('Limite deve estar entre 1 e 100');

    await expect(
      feedService.getUserFeed('user-123', 1, 101)
    ).rejects.toThrow('Limite deve estar entre 1 e 100');
  });

  test('deve retornar apenas posts de grupos que o usuário é membro', async () => {
    const mockPosts = [
      {
        id: 'post-1',
        conteudo: 'Post em grupo membro',
        group_name: 'Grupo Membro'
      }
    ];

    db.all.mockResolvedValueOnce(mockPosts);
    db.get.mockResolvedValueOnce({ total: 1 });

    const result = await feedService.getUserFeed('user-123', 1, 20);

    expect(result.posts.length).toBe(1);
    // Verificar que a query usa subquery para grupos do usuário
    expect(db.all).toHaveBeenCalledWith(
      expect.stringContaining('SELECT group_id FROM group_members WHERE user_id = ?'),
      expect.any(Array)
    );
  });
});
