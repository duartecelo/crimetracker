/**
 * Testes unitários para groupService.js
 */

const groupService = require('../../../services/groupService');
const db = require('../../../database');

// Mock do banco de dados
jest.mock('../../../database');

describe('GroupService - createGroup', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve criar grupo com sucesso', async () => {
    const mockGroup = {
      id: 'group-123',
      nome: 'Grupo Teste',
      descricao: 'Descrição do grupo',
      criador: 'user-123',
      created_at: '2024-01-15T10:00:00.000Z',
      criador_username: 'usuario',
      member_count: 1
    };

    db.get.mockResolvedValueOnce(null); // nome não existe
    db.run.mockResolvedValueOnce({}); // insert grupo
    db.run.mockResolvedValueOnce({}); // insert member
    db.get.mockResolvedValueOnce(mockGroup); // getGroupById

    const result = await groupService.createGroup(
      'user-123',
      'Grupo Teste',
      'Descrição do grupo'
    );

    expect(result.nome).toBe('Grupo Teste');
    expect(result.descricao).toBe('Descrição do grupo');
    expect(result.member_count).toBe(1);
    expect(db.run).toHaveBeenCalledTimes(2); // insert grupo + insert member
  });

  test('deve criar grupo sem descrição', async () => {
    const mockGroup = {
      id: 'group-123',
      nome: 'Grupo Teste',
      descricao: null,
      criador: 'user-123',
      created_at: '2024-01-15T10:00:00.000Z',
      member_count: 1
    };

    db.get.mockResolvedValueOnce(null);
    db.run.mockResolvedValueOnce({});
    db.run.mockResolvedValueOnce({});
    db.get.mockResolvedValueOnce(mockGroup);

    const result = await groupService.createGroup('user-123', 'Grupo Teste');

    expect(result.nome).toBe('Grupo Teste');
    expect(result.descricao).toBe(null);
  });

  test('deve rejeitar nome vazio', async () => {
    await expect(
      groupService.createGroup('user-123', '', 'Descrição')
    ).rejects.toThrow('Nome do grupo é obrigatório');
  });

  test('deve rejeitar nome muito longo', async () => {
    const longName = 'a'.repeat(101);
    await expect(
      groupService.createGroup('user-123', longName, 'Descrição')
    ).rejects.toThrow('no máximo 100 caracteres');
  });

  test('deve rejeitar nome duplicado', async () => {
    db.get.mockResolvedValueOnce({ id: 'existing-group' }); // nome já existe

    await expect(
      groupService.createGroup('user-123', 'Grupo Existente', 'Descrição')
    ).rejects.toThrow('Já existe um grupo com este nome');
  });

  test('deve adicionar criador como membro automaticamente', async () => {
    db.get.mockResolvedValueOnce(null);
    db.run.mockResolvedValueOnce({});
    db.run.mockResolvedValueOnce({});
    db.get.mockResolvedValueOnce({ id: 'group-123', member_count: 1 });

    await groupService.createGroup('user-123', 'Grupo Teste', 'Descrição');

    // Verificar se insert de member foi chamado
    expect(db.run).toHaveBeenCalledWith(
      expect.stringContaining('INSERT INTO group_members'),
      expect.arrayContaining(['user-123'])
    );
  });
});

describe('GroupService - searchGroups', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve retornar todos os grupos sem filtro', async () => {
    const mockGroups = [
      {
        id: 'group-1',
        nome: 'Grupo A',
        descricao: 'Descrição A',
        member_count: 5
      },
      {
        id: 'group-2',
        nome: 'Grupo B',
        descricao: 'Descrição B',
        member_count: 3
      }
    ];

    db.all.mockResolvedValueOnce(mockGroups);

    const result = await groupService.searchGroups();

    expect(Array.isArray(result)).toBe(true);
    expect(result.length).toBe(2);
  });

  test('deve filtrar grupos por termo de busca', async () => {
    const mockGroups = [
      {
        id: 'group-1',
        nome: 'Grupo Centro',
        descricao: 'Descrição',
        member_count: 5
      }
    ];

    db.all.mockResolvedValueOnce(mockGroups);

    const result = await groupService.searchGroups('Centro');

    expect(result.length).toBe(1);
    expect(result[0].nome).toBe('Grupo Centro');
  });

  test('deve retornar array vazio se não houver grupos', async () => {
    db.all.mockResolvedValueOnce([]);

    const result = await groupService.searchGroups();

    expect(Array.isArray(result)).toBe(true);
    expect(result.length).toBe(0);
  });
});

describe('GroupService - getGroupById', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve retornar grupo por ID', async () => {
    const mockGroup = {
      id: 'group-123',
      nome: 'Grupo Teste',
      descricao: 'Descrição',
      member_count: 5
    };

    db.get.mockResolvedValueOnce(mockGroup);

    const result = await groupService.getGroupById('group-123');

    expect(result.id).toBe('group-123');
    expect(result.nome).toBe('Grupo Teste');
  });

  test('deve rejeitar ID não encontrado', async () => {
    db.get.mockResolvedValueOnce(null);

    await expect(
      groupService.getGroupById('group-inexistente')
    ).rejects.toThrow('Grupo não encontrado');
  });
});

describe('GroupService - joinGroup', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve adicionar usuário ao grupo', async () => {
    const mockGroup = { id: 'group-123' };
    const mockUpdatedGroup = {
      id: 'group-123',
      nome: 'Grupo Teste',
      member_count: 2
    };

    db.get.mockResolvedValueOnce(mockGroup); // grupo existe
    db.get.mockResolvedValueOnce(null); // não é membro ainda
    db.run.mockResolvedValueOnce({}); // insert member
    db.get.mockResolvedValueOnce(mockUpdatedGroup); // getGroupById

    const result = await groupService.joinGroup('group-123', 'user-456');

    expect(result.member_count).toBe(2);
    expect(db.run).toHaveBeenCalledWith(
      expect.stringContaining('INSERT INTO group_members'),
      expect.any(Array)
    );
  });

  test('deve rejeitar se grupo não existir', async () => {
    db.get.mockResolvedValueOnce(null);

    await expect(
      groupService.joinGroup('group-inexistente', 'user-123')
    ).rejects.toThrow('Grupo não encontrado');
  });

  test('deve rejeitar se usuário já for membro', async () => {
    db.get.mockResolvedValueOnce({ id: 'group-123' });
    db.get.mockResolvedValueOnce({ group_id: 'group-123', user_id: 'user-123' });

    await expect(
      groupService.joinGroup('group-123', 'user-123')
    ).rejects.toThrow('Você já é membro deste grupo');
  });
});

describe('GroupService - leaveGroup', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve remover usuário do grupo', async () => {
    const mockGroup = {
      id: 'group-123',
      criador: 'user-criador'
    };
    const mockMembership = {
      group_id: 'group-123',
      user_id: 'user-456'
    };
    const mockUpdatedGroup = {
      id: 'group-123',
      nome: 'Grupo Teste',
      member_count: 1
    };

    db.get.mockResolvedValueOnce(mockGroup); // grupo existe
    db.get.mockResolvedValueOnce(mockMembership); // é membro
    db.run.mockResolvedValueOnce({}); // delete member
    db.get.mockResolvedValueOnce(mockUpdatedGroup); // getGroupById

    const result = await groupService.leaveGroup('group-123', 'user-456');

    expect(result.member_count).toBe(1);
    expect(db.run).toHaveBeenCalledWith(
      expect.stringContaining('DELETE FROM group_members'),
      ['group-123', 'user-456']
    );
  });

  test('deve rejeitar se for o criador', async () => {
    const mockGroup = {
      id: 'group-123',
      criador: 'user-criador'
    };

    db.get.mockResolvedValueOnce(mockGroup);

    await expect(
      groupService.leaveGroup('group-123', 'user-criador')
    ).rejects.toThrow('O criador não pode sair do grupo');
  });

  test('deve rejeitar se não for membro', async () => {
    const mockGroup = {
      id: 'group-123',
      criador: 'user-criador'
    };

    db.get.mockResolvedValueOnce(mockGroup);
    db.get.mockResolvedValueOnce(null); // não é membro

    await expect(
      groupService.leaveGroup('group-123', 'user-456')
    ).rejects.toThrow('Você não é membro deste grupo');
  });
});

describe('GroupService - getGroupMembers', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve retornar membros do grupo', async () => {
    const mockGroup = { id: 'group-123' };
    const mockMembers = [
      {
        id: 'user-1',
        username: 'usuario1',
        email: 'user1@exemplo.com',
        joined_at: '2024-01-15T10:00:00.000Z',
        is_creator: 1
      },
      {
        id: 'user-2',
        username: 'usuario2',
        email: 'user2@exemplo.com',
        joined_at: '2024-01-16T10:00:00.000Z',
        is_creator: 0
      }
    ];

    db.get.mockResolvedValueOnce(mockGroup);
    db.all.mockResolvedValueOnce(mockMembers);

    const result = await groupService.getGroupMembers('group-123');

    expect(Array.isArray(result)).toBe(true);
    expect(result.length).toBe(2);
    expect(result[0].is_creator).toBe(1);
  });

  test('deve rejeitar se grupo não existir', async () => {
    db.get.mockResolvedValueOnce(null);

    await expect(
      groupService.getGroupMembers('group-inexistente')
    ).rejects.toThrow('Grupo não encontrado');
  });
});

describe('GroupService - deleteGroup', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve deletar grupo com sucesso', async () => {
    const mockGroup = {
      id: 'group-123',
      criador: 'user-criador'
    };

    db.get.mockResolvedValueOnce(mockGroup);
    db.run.mockResolvedValueOnce({});

    const result = await groupService.deleteGroup('group-123', 'user-criador');

    expect(result).toBe(true);
    expect(db.run).toHaveBeenCalledWith(
      'DELETE FROM groups WHERE id = ?',
      ['group-123']
    );
  });

  test('deve rejeitar se não for o criador', async () => {
    const mockGroup = {
      id: 'group-123',
      criador: 'user-criador'
    };

    db.get.mockResolvedValueOnce(mockGroup);

    await expect(
      groupService.deleteGroup('group-123', 'user-outro')
    ).rejects.toThrow('Apenas o criador pode deletar o grupo');
  });

  test('deve rejeitar se grupo não existir', async () => {
    db.get.mockResolvedValueOnce(null);

    await expect(
      groupService.deleteGroup('group-inexistente', 'user-123')
    ).rejects.toThrow('Grupo não encontrado');
  });
});

describe('GroupService - updateGroup', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve atualizar grupo com sucesso', async () => {
    const mockGroup = {
      id: 'group-123',
      criador: 'user-criador',
      nome: 'Nome Antigo'
    };

    const mockUpdatedGroup = {
      id: 'group-123',
      nome: 'Novo Nome',
      descricao: 'Nova Descrição'
    };

    db.get.mockResolvedValueOnce(mockGroup); // verificar permissão
    db.get.mockResolvedValueOnce(null); // nome não existe
    db.run.mockResolvedValueOnce({}); // update
    db.get.mockResolvedValueOnce(mockUpdatedGroup); // getGroupById

    const result = await groupService.updateGroup(
      'group-123',
      'user-criador',
      { nome: 'Novo Nome', descricao: 'Nova Descrição' }
    );

    expect(result.nome).toBe('Novo Nome');
    expect(result.descricao).toBe('Nova Descrição');
  });

  test('deve rejeitar se não for o criador', async () => {
    db.get.mockResolvedValueOnce(null);

    await expect(
      groupService.updateGroup('group-123', 'user-outro', { nome: 'Novo Nome' })
    ).rejects.toThrow('não tem permissão');
  });

  test('deve rejeitar nome duplicado', async () => {
    const mockGroup = {
      id: 'group-123',
      criador: 'user-criador'
    };

    db.get.mockResolvedValueOnce(mockGroup);
    db.get.mockResolvedValueOnce({ id: 'group-outro' }); // nome já existe

    await expect(
      groupService.updateGroup('group-123', 'user-criador', { nome: 'Nome Existente' })
    ).rejects.toThrow('Já existe um grupo com este nome');
  });

  test('deve rejeitar atualização sem campos', async () => {
    const mockGroup = {
      id: 'group-123',
      criador: 'user-criador'
    };

    db.get.mockResolvedValueOnce(mockGroup);

    await expect(
      groupService.updateGroup('group-123', 'user-criador', {})
    ).rejects.toThrow('Nenhum campo para atualizar');
  });
});

describe('GroupService - isMember', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve retornar true se for membro', async () => {
    db.get.mockResolvedValueOnce({ group_id: 'group-123', user_id: 'user-123' });

    const result = await groupService.isMember('group-123', 'user-123');

    expect(result).toBe(true);
  });

  test('deve retornar false se não for membro', async () => {
    db.get.mockResolvedValueOnce(null);

    const result = await groupService.isMember('group-123', 'user-456');

    expect(result).toBe(false);
  });
});
