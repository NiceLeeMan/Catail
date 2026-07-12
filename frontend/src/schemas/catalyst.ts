import { z } from 'zod';

const basicInfoShape = {
  title: z.string().trim().min(1, '카탈리스트 이름을 입력하세요.').max(50, '50자 이내로 입력하세요.'),
  content: z.string().trim().min(50, '50자 이상 입력하세요.').max(500, '500자 이내로 입력하세요.'),
  industryIds: z
    .array(z.number())
    .min(1, '관련 산업을 1개 이상 선택하세요.')
    .max(10, '관련 산업은 최대 10개까지 선택할 수 있습니다.'),
};

export const createCatalystSchema = z.object({
  ...basicInfoShape,
  status: z.enum(['ACTIVE', 'INACTIVE']),
});

export type CreateCatalystFormValues = z.infer<typeof createCatalystSchema>;

export const updateCatalystBasicInfoSchema = z.object(basicInfoShape);

export type UpdateCatalystBasicInfoFormValues = z.infer<typeof updateCatalystBasicInfoSchema>;
