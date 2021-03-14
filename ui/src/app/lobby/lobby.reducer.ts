import {createReducer, on} from '@ngrx/store';
import {goToRoom, setIdentification} from './lobby.actions';

export interface User {
  id: string,
  name: string
}

export interface LobbyState {
  messages: any[],
  user: User,
  roomId: string
}

const initialState: LobbyState = {
  messages: [],
  user: null,
  roomId: null
};

export const lobbyReducer = createReducer(
  initialState,
  on(setIdentification, (state, user) => ({...state, user})),
  on(goToRoom, (state, {roomId}) => ({...state, roomId: roomId}))
);
