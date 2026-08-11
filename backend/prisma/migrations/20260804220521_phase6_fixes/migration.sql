-- AlterEnum
ALTER TYPE "NotificationType" ADD VALUE 'GROUP_ADDED';

-- AlterTable
ALTER TABLE "notifications" ADD COLUMN     "groupId" TEXT;

-- AlterTable
ALTER TABLE "tasks" ADD COLUMN     "completionPercent" INTEGER NOT NULL DEFAULT 0;
