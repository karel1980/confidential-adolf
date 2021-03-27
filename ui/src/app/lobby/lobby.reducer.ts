import {createReducer, on} from '@ngrx/store';
import {goToRoom, identified, requestRoomSync, setUser, syncRoom, userAdded} from './lobby.actions';

export interface RoomState {
  messages: any[],
  user: User,
  roomId: string,
  room: Room,
  identified: boolean,
  lastSyncRequest: Date
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

export interface Room {
  users: User[],
  game: GameTO
}

export interface GameTO {
  players: UserTO[],
  rounds: RoundTO[],
  liberalPolicies: number,
  fascistPolicies: number,
  phase: string,
  winner: string
}

export interface RoundTO {
  roundNumber: string,
  president: string,
  chancellor: string
}

function createRoom(): Room {
  return {
    users: [],
    game: createGame()
  };
}

function createGame(): GameTO {
  return {
    players: [],
    rounds: [],
    liberalPolicies: 0,
    fascistPolicies: 0,
    phase: null,
    winner: null,
  }
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
  lastSyncRequest: null
};

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
  on(syncRoom, (state, room) => ({...state, room})),
  on(userAdded, (state, user) => ({...state, room: { ...state.room, users: [...state.room.users, user ]}}))
);
