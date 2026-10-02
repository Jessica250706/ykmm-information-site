// src/constants/person.ts

export const PERSON_TYPE = {
  IDOL: 1,
  AGENT: 2,
} as const

export type PersonTypeValue = (typeof PERSON_TYPE)[keyof typeof PERSON_TYPE]

export const PERSON_TYPE_LABEL: Record<PersonTypeValue, string> = {
  [PERSON_TYPE.IDOL]: '偶像',
  [PERSON_TYPE.AGENT]: '经纪人',
}

export const PERSON_TYPE_OPTIONS = [
  { value: PERSON_TYPE.IDOL, label: PERSON_TYPE_LABEL[PERSON_TYPE.IDOL] },
  { value: PERSON_TYPE.AGENT, label: PERSON_TYPE_LABEL[PERSON_TYPE.AGENT] },
] as const

/* -------- 血型 -------- */
export const BLOOD_TYPE = {
  A: 1,
  B: 2,
  O: 3,
  AB: 4,
  OTHER: 5,
} as const

export type BloodTypeValue = (typeof BLOOD_TYPE)[keyof typeof BLOOD_TYPE]

export const BLOOD_TYPE_LABEL: Record<BloodTypeValue, string> = {
  [BLOOD_TYPE.A]: 'A型',
  [BLOOD_TYPE.B]: 'B型',
  [BLOOD_TYPE.O]: 'O型',
  [BLOOD_TYPE.AB]: 'AB型',
  [BLOOD_TYPE.OTHER]: '其他',
}

export const BLOOD_TYPE_OPTIONS = [
  { value: BLOOD_TYPE.A, label: BLOOD_TYPE_LABEL[BLOOD_TYPE.A] },
  { value: BLOOD_TYPE.B, label: BLOOD_TYPE_LABEL[BLOOD_TYPE.B] },
  { value: BLOOD_TYPE.O, label: BLOOD_TYPE_LABEL[BLOOD_TYPE.O] },
  { value: BLOOD_TYPE.AB, label: BLOOD_TYPE_LABEL[BLOOD_TYPE.AB] },
  { value: BLOOD_TYPE.OTHER, label: BLOOD_TYPE_LABEL[BLOOD_TYPE.OTHER] },
] as const
