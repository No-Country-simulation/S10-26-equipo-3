import SolicitudesPage from "../../pages/admin/SolicitudesPage";

type Props = {
  token: string;
  onLogout: () => void;
};

export default function SolicitudesPendientes({ token }: Props) {
  return <SolicitudesPage token={token} dentroDelPanel />;
}
