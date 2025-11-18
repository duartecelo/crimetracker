/**
 * Testes unitários para reportService.js
 */

const reportService = require('../../../services/reportService');
const db = require('../../../database');

// Mock do banco de dados
jest.mock('../../../database');

describe('ReportService - createReport', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve criar denúncia com sucesso', async () => {
    const mockReport = {
      id: 'report-123',
      tipo: 'Assalto',
      descricao: 'Descrição do assalto',
      lat: -23.5505,
      lon: -46.6333,
      created_at: '2024-01-15T10:00:00.000Z',
      author_username: 'usuario'
    };

    db.run.mockResolvedValueOnce({});
    db.get.mockResolvedValueOnce(mockReport);

    const result = await reportService.createReport(
      'user-123',
      'Assalto',
      'Descrição do assalto',
      -23.5505,
      -46.6333
    );

    expect(result.tipo).toBe('Assalto');
    expect(result.descricao).toBe('Descrição do assalto');
    expect(result.lat).toBe(-23.5505);
    expect(result.lon).toBe(-46.6333);
    expect(db.run).toHaveBeenCalledTimes(1);
  });

  test('deve rejeitar tipo de crime inválido', async () => {
    await expect(
      reportService.createReport('user-123', 'TipoInvalido', 'Descrição', -23.5505, -46.6333)
    ).rejects.toThrow('Tipo de crime inválido');
  });

  test('deve rejeitar descrição vazia', async () => {
    await expect(
      reportService.createReport('user-123', 'Assalto', '', -23.5505, -46.6333)
    ).rejects.toThrow('Descrição é obrigatória');
  });

  test('deve rejeitar descrição muito longa', async () => {
    const longDescription = 'a'.repeat(501);
    await expect(
      reportService.createReport('user-123', 'Assalto', longDescription, -23.5505, -46.6333)
    ).rejects.toThrow('no máximo 500 caracteres');
  });

  test('deve rejeitar latitude inválida', async () => {
    await expect(
      reportService.createReport('user-123', 'Assalto', 'Descrição', 91, -46.6333)
    ).rejects.toThrow('Latitude inválida');

    await expect(
      reportService.createReport('user-123', 'Assalto', 'Descrição', -91, -46.6333)
    ).rejects.toThrow('Latitude inválida');
  });

  test('deve rejeitar longitude inválida', async () => {
    await expect(
      reportService.createReport('user-123', 'Assalto', 'Descrição', -23.5505, 181)
    ).rejects.toThrow('Longitude inválida');

    await expect(
      reportService.createReport('user-123', 'Assalto', 'Descrição', -23.5505, -181)
    ).rejects.toThrow('Longitude inválida');
  });

  test('deve aceitar tipos de crime case-insensitive', async () => {
    const mockReport = {
      id: 'report-123',
      tipo: 'furto',
      descricao: 'Descrição',
      lat: -23.5505,
      lon: -46.6333,
      created_at: '2024-01-15T10:00:00.000Z',
      author_username: 'usuario'
    };

    db.run.mockResolvedValueOnce({});
    db.get.mockResolvedValueOnce(mockReport);

    await expect(
      reportService.createReport('user-123', 'FURTO', 'Descrição', -23.5505, -46.6333)
    ).resolves.toBeDefined();
  });
});

describe('ReportService - getNearbyReports', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve retornar denúncias próximas', async () => {
    const mockReports = [
      {
        id: 'report-1',
        tipo: 'Assalto',
        descricao: 'Denúncia 1',
        lat: -23.5505,
        lon: -46.6333,
        created_at: new Date().toISOString(),
        author_username: 'user1'
      },
      {
        id: 'report-2',
        tipo: 'Furto',
        descricao: 'Denúncia 2',
        lat: -23.5510,
        lon: -46.6340,
        created_at: new Date().toISOString(),
        author_username: 'user2'
      }
    ];

    db.all.mockResolvedValueOnce(mockReports);

    const result = await reportService.getNearbyReports(-23.5505, -46.6333, 5);

    expect(Array.isArray(result)).toBe(true);
    expect(result.length).toBeLessThanOrEqual(2);
    expect(result[0]).toHaveProperty('distance_meters');
    expect(result[0]).toHaveProperty('distance_km');
  });

  test('deve filtrar por raio especificado', async () => {
    const mockReports = [
      {
        id: 'report-1',
        tipo: 'Assalto',
        descricao: 'Denúncia próxima',
        lat: -23.5505,
        lon: -46.6333,
        created_at: new Date().toISOString(),
        author_username: 'user1'
      },
      {
        id: 'report-2',
        tipo: 'Furto',
        descricao: 'Denúncia muito distante',
        lat: -22.9068,
        lon: -43.1729, // Rio de Janeiro - muito longe
        created_at: new Date().toISOString(),
        author_username: 'user2'
      }
    ];

    db.all.mockResolvedValueOnce(mockReports);

    const result = await reportService.getNearbyReports(-23.5505, -46.6333, 1); // 1km de raio

    expect(result.length).toBe(1);
    expect(result[0].id).toBe('report-1');
  });

  test('deve rejeitar coordenadas inválidas', async () => {
    await expect(
      reportService.getNearbyReports(91, -46.6333, 5)
    ).rejects.toThrow('Latitude inválida');

    await expect(
      reportService.getNearbyReports(-23.5505, 181, 5)
    ).rejects.toThrow('Longitude inválida');
  });

  test('deve rejeitar raio inválido', async () => {
    await expect(
      reportService.getNearbyReports(-23.5505, -46.6333, -5)
    ).rejects.toThrow('Raio deve ser um número positivo');

    await expect(
      reportService.getNearbyReports(-23.5505, -46.6333, 0)
    ).rejects.toThrow('Raio deve ser um número positivo');
  });
});

describe('ReportService - getReportById', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve retornar denúncia por ID', async () => {
    const mockReport = {
      id: 'report-123',
      tipo: 'Assalto',
      descricao: 'Descrição',
      lat: -23.5505,
      lon: -46.6333,
      created_at: '2024-01-15T10:00:00.000Z',
      author_username: 'usuario'
    };

    db.get.mockResolvedValueOnce(mockReport);

    const result = await reportService.getReportById('report-123');

    expect(result.id).toBe('report-123');
    expect(result.tipo).toBe('Assalto');
  });

  test('deve rejeitar ID não encontrado', async () => {
    db.get.mockResolvedValueOnce(null);

    await expect(
      reportService.getReportById('report-inexistente')
    ).rejects.toThrow('Denúncia não encontrada');
  });
});

describe('ReportService - getUserReports', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve retornar denúncias do usuário', async () => {
    const mockReports = [
      {
        id: 'report-1',
        tipo: 'Assalto',
        descricao: 'Denúncia 1',
        lat: -23.5505,
        lon: -46.6333,
        created_at: '2024-01-15T10:00:00.000Z'
      },
      {
        id: 'report-2',
        tipo: 'Furto',
        descricao: 'Denúncia 2',
        lat: -23.5510,
        lon: -46.6340,
        created_at: '2024-01-14T10:00:00.000Z'
      }
    ];

    db.all.mockResolvedValueOnce(mockReports);

    const result = await reportService.getUserReports('user-123');

    expect(Array.isArray(result)).toBe(true);
    expect(result.length).toBe(2);
    expect(result[0].id).toBe('report-1');
  });

  test('deve retornar array vazio se usuário não tem denúncias', async () => {
    db.all.mockResolvedValueOnce([]);

    const result = await reportService.getUserReports('user-sem-denuncias');

    expect(Array.isArray(result)).toBe(true);
    expect(result.length).toBe(0);
  });
});

describe('ReportService - updateReport', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve atualizar denúncia com sucesso', async () => {
    const mockReport = {
      id: 'report-123',
      user_id: 'user-123',
      tipo: 'Assalto',
      descricao: 'Descrição antiga'
    };

    const mockUpdatedReport = {
      id: 'report-123',
      tipo: 'Furto',
      descricao: 'Descrição atualizada',
      lat: -23.5505,
      lon: -46.6333,
      created_at: '2024-01-15T10:00:00.000Z'
    };

    db.get.mockResolvedValueOnce(mockReport);
    db.run.mockResolvedValueOnce({});
    db.get.mockResolvedValueOnce(mockUpdatedReport);

    const result = await reportService.updateReport(
      'report-123',
      'user-123',
      { tipo: 'Furto', descricao: 'Descrição atualizada' }
    );

    expect(result.tipo).toBe('Furto');
    expect(result.descricao).toBe('Descrição atualizada');
  });

  test('deve rejeitar atualização de denúncia de outro usuário', async () => {
    db.get.mockResolvedValueOnce(null);

    await expect(
      reportService.updateReport('report-123', 'user-outro', { tipo: 'Furto' })
    ).rejects.toThrow('não tem permissão');
  });

  test('deve rejeitar tipo inválido na atualização', async () => {
    const mockReport = {
      id: 'report-123',
      user_id: 'user-123',
      tipo: 'Assalto'
    };

    db.get.mockResolvedValueOnce(mockReport);

    await expect(
      reportService.updateReport('report-123', 'user-123', { tipo: 'TipoInvalido' })
    ).rejects.toThrow('Tipo de crime inválido');
  });

  test('deve rejeitar atualização sem campos', async () => {
    const mockReport = {
      id: 'report-123',
      user_id: 'user-123'
    };

    db.get.mockResolvedValueOnce(mockReport);

    await expect(
      reportService.updateReport('report-123', 'user-123', {})
    ).rejects.toThrow('Nenhum campo para atualizar');
  });
});

describe('ReportService - deleteReport', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve deletar denúncia com sucesso', async () => {
    const mockReport = {
      id: 'report-123',
      user_id: 'user-123'
    };

    db.get.mockResolvedValueOnce(mockReport);
    db.run.mockResolvedValueOnce({});

    const result = await reportService.deleteReport('report-123', 'user-123');

    expect(result).toBe(true);
    expect(db.run).toHaveBeenCalledWith(
      'DELETE FROM crime_reports WHERE id = ?',
      ['report-123']
    );
  });

  test('deve rejeitar deleção de denúncia de outro usuário', async () => {
    db.get.mockResolvedValueOnce(null);

    await expect(
      reportService.deleteReport('report-123', 'user-outro')
    ).rejects.toThrow('não tem permissão');
  });
});
