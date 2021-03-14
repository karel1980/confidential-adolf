import {createAction, props} from '@ngrx/store';

export const setIdentification = createAction(
  'setIdentification',
  props<{id: string, name: string}>());

export const addChatMessage = createAction(
  'addChatMessage',
  props<{ userIdi: string, userName: string, message: string }>());

