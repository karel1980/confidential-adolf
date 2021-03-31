import {createReducer, on} from '@ngrx/store';
import {goToRoom, identified, requestRoomSync, setLastError, setUser, syncRoom, userAdded} from './lobby.actions';

export interface RoomState {
  messages: any[],
  user: User,
  roomId: string,
  room: Room,
  identified: boolean,
  lastSyncRequest: Date,
  lastError: ErrorResponse
}

export interface User {
  id: string,
  name: string
}

export interface UserTO {
  id: string,
  name: string,
  dead: boolean,
  hitler: boolean
}

export interface RoomTO {
  users: User[],
  game: GameTO
}

export interface GameTO {
  players: UserTO[],
  rounds: RoundTO[],
  failedElections: number,
  liberalPolicies: number,
  fascistPolicies: number,
  phase: string,
  winner: string,
  hitler: string,
  fascistTiles: { executiveAction: ExecutivePower }[]
}

export interface RoundTO {
  roundNumber: string,
  president: string,
  chancellor: string,
  presidentPolicies: PolicyTile[],
  chancellorPolicies: PolicyTile[],
  playerVoted: boolean,
  executivePower: ExecutivePower,
  peekedTiles: PolicyTile[];
  investigationResult: {
    player: string,
    result: PolicyTile
  }
}

function createRoom(): Room {
  return {
    users: [],
    game: null
  };
}

const initialState: RoomState = {
  messages: [],
  user: {
    id: null,
    name: null
  },
  roomId: null,
  room: createRoom(),
  identified: false,
  lastSyncRequest: null,
  lastError: null
};

export interface ErrorResponse {
  code: string,
  data: any
}

export const lobbyReducer = createReducer(
  initialState,
  on(setUser, (state, user) => {
    localStorage.setItem('user', JSON.stringify(user));
    return ({...state, user});
  }),
  on(goToRoom, (state, {roomId}) => ({...state, roomId: roomId, room: createRoom()})),
  on(identified, (state, user) => ({...state, user:user, identified: true})),
  on(requestRoomSync, (state) => ({...state, lastSyncRequest: new Date()})),
  on(syncRoom, (state, room) => ({...state, room: buildRoom(room, state.user != null ? state.user.id : null)})),
  on(userAdded, (state, user) => ({...state, room: {...state.room, users: [...state.room.users, user]}})),
  on(setLastError, (state, errorResponse) => ({...state, lastError: errorResponse}))
);

export enum Vote {
  YES = 'YES',
  NO = 'NO'
}

export enum PolicyTile {
  LIBERAL = 'LIBERAL',
  FASCIST = 'FASCIST'
}

export enum ExecutivePower {
  INVESTIGATE_LOYALTY = 'INVESTIGATE_LOYALTY',
  CALL_SPECIAL_ELECTION = 'CALL_SPECIAL_ELECTION',
  POLICY_PEEK = 'POLICY_PEEK',
  EXECUTION = 'EXECUTION'
}

export interface Room extends RoomTO {
  game: Game
}

export interface Game extends GameTO {
  fullPlayers: Player[],
  failedElections: number,
  fascistLane: Lane,
  liberalLane: void[],
  currentRound: RoundTO,
  askNominateChancellor: boolean,
  askVoteLeadership: boolean,
  playerVoted: boolean,
  askDiscardPolicy: boolean,
  policiesToDiscard: PolicyTile[]
}

export interface Player {
  id: string,
  name: string,
  president: boolean,
  chancellor: boolean,
  hitler?: boolean,
  dead: boolean
}

export interface Lane {
  faction: PolicyTile,
  tiles: { executivePower: ExecutivePower }[]
}

function buildRoom(room: RoomTO, userId: string): Room {
  if (!room.game) {
    return <Room>room;
  }

  return ({
    ...room,
    game: buildGame(room.game, userId)
  })
}

export enum GamePhase {
  NOMINATING_CHANCELLOR = 'NOMINATING_CHANCELLOR',
  VOTING_LEADERSHIP = 'VOTING_LEADERSHIP',
  PRESIDENT_DISCARDS_POLICY_TILE = 'PRESIDENT_DISCARDS_POLICY_TILE',
  CHANCELLOR_DISCARDS_POLICY_TILE = 'CHANCELLOR_DISCARDS_POLICY_TILE'
}

function buildGame(game: GameTO, userId: string): Game {
  let currentRound = game.rounds[game.rounds.length-1];
  let presidentDiscarding = game.phase == GamePhase.PRESIDENT_DISCARDS_POLICY_TILE && currentRound.president == userId;
  let chancellorDiscarding = game.phase == GamePhase.CHANCELLOR_DISCARDS_POLICY_TILE && currentRound.chancellor == userId;

  let policiesToDiscard = [];
  if (presidentDiscarding) {
    policiesToDiscard = currentRound.presidentPolicies;
  }
  if (chancellorDiscarding) {
    policiesToDiscard = currentRound.chancellorPolicies;
  }

  return ({
    ...game,
    fullPlayers: buildFullPlayers(game),
    fascistLane: {
      faction: PolicyTile.FASCIST,
      tiles: game.fascistTiles.map(it => ({executivePower: it.executiveAction}))
    },
    liberalLane: Array(game.liberalPolicies),
    currentRound: currentRound,
    playerVoted: currentRound.playerVoted,
    askNominateChancellor: game.phase == GamePhase.NOMINATING_CHANCELLOR && currentRound.president== userId,
    askVoteLeadership: game.phase == GamePhase.VOTING_LEADERSHIP,
    askDiscardPolicy: presidentDiscarding || chancellorDiscarding,
    policiesToDiscard: policiesToDiscard
  })
}
function buildFullPlayers(game: GameTO): Player[] {
  return game.players.map(p => ({
    id: p.id,
    name: p.name,
    dead: p.dead,
    president: game.rounds[game.rounds.length - 1].president == p.id,
    chancellor: game.rounds[game.rounds.length - 1].chancellor == p.id,
    hitler: p.hitler
  }));
}
