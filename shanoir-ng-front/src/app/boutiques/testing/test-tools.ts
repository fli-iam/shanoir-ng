import { ToolInfo } from './tool.model';

/** return fresh array of test tools */
export function getTestTools(): ToolInfo[] {
  return [
    {id: 41, name: 'Bob', description: 'b', nDownloads: 41 },
    {id: 42, name: 'Carol', description: 'c', nDownloads: 42 },
    {id: 43, name: 'Ted', description: 't', nDownloads: 43 },
    {id: 44, name: 'Alice', description: 'a', nDownloads: 44 },
    {id: 45, name: 'Speedy', description: 's', nDownloads: 45 },
    {id: 46, name: 'Stealthy', description: 's', nDownloads: 46 }
  ];
}
