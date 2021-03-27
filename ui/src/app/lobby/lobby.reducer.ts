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
  dead: boolean
}

export interface RoomTO {
  users: User[],
  game: GameTO
}

export interface GameTO {
  players: UserTO[],
  rounds: RoundTO[],
  liberalPolicies: number,
  fascistPolicies: number,
  phase: string,
  winner: string,
  fascistTiles: { executiveAction: ExecutivePower }[]
}

export interface RoundTO {
  roundNumber: string,
  president: string,
  chancellor: string
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
    console.log('setting this in local storage', user);
    localStorage.setItem('user', JSON.stringify(user));
    return ({...state, user});
  }),
  on(goToRoom, (state, {roomId}) => ({...state, roomId: roomId, room: createRoom()})),
  on(identified, (state, a) => ({...state, identified: true})),
  on(requestRoomSync, (state) => ({...state, lastSyncRequest: new Date()})),
  on(syncRoom, (state, room) => ({...state, room: buildRoom(room)})),
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

interface Room extends RoomTO {
  game: Game
}

interface Game extends GameTO {
  fascistLane: Lane,
  liberalLane: void[]
}

interface Lane {
  faction: PolicyTile,
  tiles: { executivePower: ExecutivePower }[]
}

function buildRoom(room: RoomTO): Room {
  if (!room.game) {
    return <Room>room;
  }

  return ({
    ...room,
    game: buildGame(room.game)
  })
}

function buildGame(game: GameTO): Game {
  return ({
    ...game,
    fascistLane: {
      faction: PolicyTile.FASCIST,
      tiles: game.fascistTiles.map(it => ({executivePower: it.executiveAction}))
    },
    liberalLane: Array(game.liberalPolicies)
  })
}
