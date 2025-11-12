package com.crimetracker.app.data.mapper

import com.crimetracker.app.data.local.entity.*
import com.crimetracker.app.data.model.*

// User mappers
fun User.toEntity() = UserEntity(
    id = id,
    username = username,
    email = email,
    createdAt = createdAt
)

fun UserEntity.toUser() = User(
    id = id,
    username = username,
    email = email,
    createdAt = createdAt
)

// Report mappers
fun Report.toEntity() = CrimeReportEntity(
    id = id,
    tipo = tipo,
    descricao = descricao,
    lat = lat,
    lon = lon,
    createdAt = createdAt,
    authorUsername = authorUsername,
    distanceMeters = distanceMeters,
    distanceKm = distanceKm
)

fun CrimeReportEntity.toReport() = Report(
    id = id,
    tipo = tipo,
    descricao = descricao,
    lat = lat,
    lon = lon,
    createdAt = createdAt,
    authorUsername = authorUsername,
    distanceMeters = distanceMeters,
    distanceKm = distanceKm
)

// Group mappers
fun Group.toEntity(isMember: Boolean = false) = GroupEntity(
    id = id,
    nome = nome,
    descricao = descricao,
    criadorUsername = criadorUsername,
    memberCount = memberCount,
    createdAt = createdAt,
    isMember = isMember
)

fun GroupEntity.toGroup() = Group(
    id = id,
    nome = nome,
    descricao = descricao,
    criadorUsername = criadorUsername,
    memberCount = memberCount,
    createdAt = createdAt
)

// Post mappers
fun Post.toEntity() = PostEntity(
    id = id,
    groupId = groupId,
    authorId = authorId,
    conteudo = conteudo,
    createdAt = createdAt,
    authorUsername = authorUsername,
    groupName = groupName
)

fun PostEntity.toPost() = Post(
    id = id,
    groupId = groupId,
    authorId = authorId,
    conteudo = conteudo,
    createdAt = createdAt,
    authorUsername = authorUsername,
    groupName = groupName
)

